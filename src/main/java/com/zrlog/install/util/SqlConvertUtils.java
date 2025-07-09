package com.zrlog.install.util;

import com.hibegin.common.util.IOUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SqlConvertUtils {

    public static List<String> extractExecutableSqlByInputStream(InputStream inputStream) {
        return extractExecutableSql(IOUtil.getStringInputStream(inputStream));
    }

    public static List<String> extractExecutableSql(String sql) {
        String[] sqlArr = sql.split("\n");
        StringBuilder tempSqlStr = new StringBuilder();
        List<String> sqlList = new ArrayList<>();
        for (String sqlSt : sqlArr) {
            if (sqlSt.startsWith("#") || sqlSt.startsWith("/*")) {
                continue;
            }
            if (sqlSt.startsWith("--")) {
                continue;
            }
            if (sqlSt.startsWith("USE")) {
                continue;
            }
            if (sqlSt.startsWith("CREATE DATABASE")) {
                continue;
            }
            if (sqlSt.startsWith("LOCK TABLES")) {
                continue;
            }
            if (sqlSt.startsWith("UNLOCK TABLES")) {
                continue;
            }
            tempSqlStr.append(sqlSt).append("\n");
        }
        String[] cleanSql = tempSqlStr.toString().split(";\n");
        for (String sqlSt : cleanSql) {
            if (StringUtils.isEmpty(sqlSt) || sqlSt.trim().isEmpty()) {
                continue;
            }
            String[] split = sqlSt.split("\n");
            StringBuilder sb = new StringBuilder();
            for (String t : split) {
                if (StringUtils.isEmpty(t) || t.trim().isEmpty()) {
                    continue;
                }
                sb.append(t);
            }
            sqlList.add(sb.toString());
        }
        return sqlList;
    }


    public static List<Object> extractValues(String sql) {
        List<Object> result = new ArrayList<>();

        // 提取 VALUES 中的参数部分
        Pattern pattern = Pattern.compile("VALUES\\s*\\((.+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        if (!matcher.find()) return result;

        String valuesPart = matcher.group(1);
        List<String> tokens = splitSqlValues(valuesPart);

        for (String token : tokens) {
            token = token.trim();
            if (token.equalsIgnoreCase("null")) {
                result.add(null);
            } else if (token.equalsIgnoreCase("true") || token.equalsIgnoreCase("false")) {
                result.add(Boolean.parseBoolean(token));
            } else if (token.startsWith("'") && token.endsWith("'")) {
                String unescaped = token.substring(1, token.length() - 1).replace("\\'", "'").replace("\\\\", "\\").replace("\\r", "\r").replace("\\n", "\n");
                result.add(unescaped);
            } else {
                // 试图解析为数字
                try {
                    if (token.contains(".")) {
                        result.add(Double.parseDouble(token));
                    } else {
                        result.add(Long.parseLong(token));
                    }
                } catch (NumberFormatException e) {
                    result.add(token); // fallback
                }
            }
        }

        return result;
    }

    // 处理 SQL 中的值（考虑逗号、引号、转义等）
    private static List<String> splitSqlValues(String input) {
        List<String> tokens = new ArrayList<>();
        boolean inQuote = false;
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c == '\'' && (i == 0 || input.charAt(i - 1) != '\\')) {
                inQuote = !inQuote;
            }

            if (c == ',' && !inQuote) {
                tokens.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        tokens.add(current.toString());
        return tokens;
    }

    public static List<String> doMySQLToSqliteBySqlText(String rawSql) {
        List<String> result = new ArrayList<>();
        for (String sqlSt : SqlConvertUtils.extractExecutableSql(rawSql)) {
            if (Objects.equals("DROP TABLE IF EXISTS `comment`, `link`, `log`, `lognav`, `plugin`, `tag`, `type`, `user`, `website`", sqlSt.trim())) {
                continue;
            }
            String cleanText = sqlSt
                    .replace("_binary '\u0001'", "true")
                    .replaceAll("FOREIGN KEY \\(`?(\\w+)`\\) REFERENCES `?(\\w+)` \\(`?(\\w+)`\\)", "")
                    .replace("_binary '\u0001'", "true")
                    .replace("_binary '\u0000'", "false")
                    .replace("_binary '\\0'", "false")
                    .replace("_binary '\\1'", "true")
                    .replace("_binary '\\x00'", "false")
                    .replace("_binary '\\x01'", "true")
                    .replace("ENGINE=InnoDB", "")
                    .replace("DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_general_ci", "")
                    .replaceAll("UNIQUE KEY `?(\\w+)` \\(`text`\\)", "UNIQUE(`text`)")
                    .replaceAll("UNIQUE KEY `?(\\w+)` \\(`name`\\)", "UNIQUE(`name`)")
                    .replaceAll("UNIQUE KEY `?(\\w+)` \\(`userName`\\)", "UNIQUE(`userName`)")
                    .replaceAll("UNIQUE KEY `?(\\w+)` \\(`alias`\\)", "UNIQUE(`alias`)")
                    .replaceAll("UNIQUE KEY `?(\\w+)` \\(`postId`\\)", "UNIQUE(`postId`)")
                    .replaceAll("KEY\\s+`?(\\w+)` \\(`pid`\\),", "")
                    .replaceAll("KEY\\s+`?(\\w+)` \\(`logId`\\),", "")
                    .replaceAll("KEY\\s+`?(\\w+)` \\(`typeId`\\),", "")
                    .replaceAll("KEY\\s+`?(\\w+)` \\(`typeId`\\),", "")
                    .replaceAll("KEY\\s+`?(\\w+)` \\(`userId`\\),", "")
                    .replaceAll("bit(1)", "BOOLEAN").replace("DEFAULT b'1'", "")
                    .replace("DEFAULT b'0'", "")
                    .replaceAll("PRIMARY\\s+KEY\\s*\\(\\s*`?(\\w+)`?\\s*\\),", "")
                    .replaceAll("PRIMARY\\s+KEY\\s*\\(\\s*`?(\\w+)`?\\s*\\)", "")
                    .replace("int(11) NOT NULL AUTO_INCREMENT", "INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT")
                    .replaceAll("COMMENT\\s*'([^']*)'", "")
                    .replace("ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE utf8mb4_unicode_ci", "")
                    .replace("ENGINE=InnoDB DEFAULT CHARSET=utf8", "")
                    .replace("DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci", "")
                    .replaceAll("AUTO_INCREMENT=\\w+", "")
                    .replace(",  )", ")");

            if (cleanText.startsWith("INSERT INTO")) {
                result.addAll(splitInsertStatements(cleanText));
            } else {
                result.add(cleanText);
            }
        }
        return result;
    }

    public static List<String> splitInsertStatements(String sql) {
        List<String> result = new ArrayList<>();

        int valuesIndex = sql.toUpperCase().indexOf("VALUES");
        if (valuesIndex == -1) {
            throw new IllegalArgumentException("SQL must contain VALUES");
        }

        String prefix = sql.substring(0, valuesIndex + "VALUES".length());
        String valuesPart = sql.substring(valuesIndex + "VALUES".length()).trim();

        // 去掉尾部 ; 分号
        if (valuesPart.endsWith(";")) {
            valuesPart = valuesPart.substring(0, valuesPart.length() - 1);
        }

        boolean inString = false;
        boolean escaping = false;
        int parenDepth = 0;

        StringBuilder current = new StringBuilder();

        for (int i = 0; i < valuesPart.length(); i++) {
            char c = valuesPart.charAt(i);
            current.append(c);

            if (escaping) {
                escaping = false;
                continue;
            }

            if (c == '\\') {
                escaping = true;
            } else if (c == '\'') {
                inString = !inString;
            } else if (!inString) {
                if (c == '(') {
                    parenDepth++;
                } else if (c == ')') {
                    parenDepth--;
                    if (parenDepth == 0) {
                        // 完整一组
                        String insertSql = (prefix + " " + current.toString().trim())
                                .replace("\\'", "\"")
                                .replace("\\\"", "\"");
                        result.add(insertSql);
                        current.setLength(0);

                        // 跳过后续逗号和空格
                        while (i + 1 < valuesPart.length() &&
                                (valuesPart.charAt(i + 1) == ',' || Character.isWhitespace(valuesPart.charAt(i + 1)))) {
                            i++;
                        }
                    }
                }
            }
        }

        return result;
    }
}
