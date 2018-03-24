package com.esotericsoftware.wildcard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

class GlobScanner {
    private final List<String> matches = new ArrayList(128);
    private final File rootDir;

    static class Pattern {
        boolean ignoreCase;
        private int index;
        String value;
        final String[] values;

        Pattern(String pattern, boolean ignoreCase) {
            this.ignoreCase = ignoreCase;
            pattern = pattern.replace('\\', '/').replaceAll("\\*\\*[^/]", "**/*").replaceAll("[^/]\\*\\*", "*/**");
            if (ignoreCase) {
                pattern = pattern.toLowerCase();
            }
            this.values = pattern.split("/");
            this.value = this.values[0];
        }

        boolean matches(String fileName) {
            if (this.value.equals("**")) {
                return true;
            }
            if (this.ignoreCase) {
                fileName = fileName.toLowerCase();
            }
            if (this.value.indexOf(42) == -1 && this.value.indexOf(63) == -1) {
                return fileName.equals(this.value);
            }
            int i = 0;
            int j = 0;
            while (i < fileName.length() && j < this.value.length() && this.value.charAt(j) != '*') {
                if (this.value.charAt(j) != fileName.charAt(i) && this.value.charAt(j) != '?') {
                    return false;
                }
                i++;
                j++;
            }
            if (j != this.value.length()) {
                int cp = 0;
                int mp = 0;
                while (i < fileName.length()) {
                    if (j < this.value.length() && this.value.charAt(j) == '*') {
                        int j2 = j + 1;
                        if (j >= this.value.length()) {
                            return true;
                        }
                        mp = j2;
                        cp = i + 1;
                        j = j2;
                    } else if (j >= this.value.length() || !(this.value.charAt(j) == fileName.charAt(i) || this.value.charAt(j) == '?')) {
                        j = mp;
                        i = cp;
                        cp++;
                    } else {
                        j++;
                        i++;
                    }
                }
                while (j < this.value.length() && this.value.charAt(j) == '*') {
                    j++;
                }
                if (j < this.value.length()) {
                    return false;
                }
                return true;
            } else if (fileName.length() != this.value.length()) {
                return false;
            } else {
                return true;
            }
        }

        String nextValue() {
            if (this.index + 1 == this.values.length) {
                return null;
            }
            return this.values[this.index + 1];
        }

        boolean incr(String fileName) {
            if (!this.value.equals("**")) {
                incr();
            } else if (this.index == this.values.length - 1) {
                return false;
            } else {
                incr();
                if (matches(fileName)) {
                    incr();
                } else {
                    decr();
                    return false;
                }
            }
            return true;
        }

        void incr() {
            this.index++;
            if (this.index >= this.values.length) {
                this.value = null;
            } else {
                this.value = this.values[this.index];
            }
        }

        void decr() {
            this.index--;
            if (this.index > 0 && this.values[this.index - 1].equals("**")) {
                this.index--;
            }
            this.value = this.values[this.index];
        }

        void reset() {
            this.index = 0;
            this.value = this.values[0];
        }

        boolean isExhausted() {
            return this.index >= this.values.length;
        }

        boolean isLast() {
            return this.index >= this.values.length + -1;
        }

        boolean wasFinalMatch() {
            return isExhausted() || (isLast() && this.value.equals("**"));
        }
    }

    public GlobScanner(File rootDir, List<String> includes, List<String> excludes, boolean ignoreCase) {
        if (rootDir == null) {
            throw new IllegalArgumentException("rootDir cannot be null.");
        } else if (!rootDir.exists()) {
            throw new IllegalArgumentException("Directory does not exist: " + rootDir);
        } else if (rootDir.isDirectory()) {
            try {
                rootDir = rootDir.getCanonicalFile();
                this.rootDir = rootDir;
                if (includes == null) {
                    throw new IllegalArgumentException("includes cannot be null.");
                } else if (excludes == null) {
                    throw new IllegalArgumentException("excludes cannot be null.");
                } else {
                    if (includes.isEmpty()) {
                        includes.add("**");
                    }
                    List<Pattern> includePatterns = new ArrayList(includes.size());
                    for (String include : includes) {
                        includePatterns.add(new Pattern(include, ignoreCase));
                    }
                    List<Pattern> allExcludePatterns = new ArrayList(excludes.size());
                    for (String exclude : excludes) {
                        allExcludePatterns.add(new Pattern(exclude, ignoreCase));
                    }
                    scanDir(rootDir, includePatterns);
                    if (!allExcludePatterns.isEmpty()) {
                        Iterator<String> matchIter = this.matches.iterator();
                        while (matchIter.hasNext()) {
                            Pattern exclude2;
                            String filePath = (String) matchIter.next();
                            List<Pattern> excludePatterns = new ArrayList(allExcludePatterns);
                            Iterator<Pattern> excludeIter = excludePatterns.iterator();
                            while (excludeIter.hasNext()) {
                                exclude2 = (Pattern) excludeIter.next();
                                if (exclude2.values.length == 2 && exclude2.values[0].equals("**")) {
                                    exclude2.incr();
                                    if (exclude2.matches(filePath.substring(filePath.lastIndexOf(File.separatorChar) + 1))) {
                                        matchIter.remove();
                                        for (Pattern exclude22 : allExcludePatterns) {
                                            exclude22.reset();
                                        }
                                    } else {
                                        try {
                                            excludeIter.remove();
                                        } catch (Throwable th) {
                                            for (Pattern exclude222 : allExcludePatterns) {
                                                exclude222.reset();
                                            }
                                        }
                                    }
                                }
                            }
                            String[] fileNames = filePath.split("\\" + File.separator);
                            int length = fileNames.length;
                            int i = 0;
                            while (i < length) {
                                String fileName = fileNames[i];
                                excludeIter = excludePatterns.iterator();
                                while (excludeIter.hasNext()) {
                                    exclude222 = (Pattern) excludeIter.next();
                                    if (exclude222.matches(fileName)) {
                                        exclude222.incr(fileName);
                                        if (exclude222.wasFinalMatch()) {
                                            matchIter.remove();
                                            for (Pattern exclude2222 : allExcludePatterns) {
                                                exclude2222.reset();
                                            }
                                        }
                                    } else {
                                        excludeIter.remove();
                                    }
                                }
                                if (excludePatterns.isEmpty()) {
                                    for (Pattern exclude22222 : allExcludePatterns) {
                                        exclude22222.reset();
                                    }
                                } else {
                                    i++;
                                }
                            }
                            for (Pattern exclude222222 : allExcludePatterns) {
                                exclude222222.reset();
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                throw new RuntimeException("OS error determining canonical path: " + rootDir, ex);
            }
        } else {
            throw new IllegalArgumentException("File must be a directory: " + rootDir);
        }
    }

    private void scanDir(File dir, List<Pattern> includes) {
        int i = 0;
        if (dir.canRead()) {
            boolean scanAll = false;
            for (Pattern include : includes) {
                if (include.value.indexOf(42) == -1) {
                    if (include.value.indexOf(63) != -1) {
                    }
                }
                scanAll = true;
            }
            List<Pattern> matchingIncludes;
            if (scanAll) {
                String[] list = dir.list();
                int length = list.length;
                while (i < length) {
                    String fileName = list[i];
                    matchingIncludes = new ArrayList(includes.size());
                    for (Pattern include2 : includes) {
                        if (include2.matches(fileName)) {
                            matchingIncludes.add(include2);
                        }
                    }
                    if (!matchingIncludes.isEmpty()) {
                        process(dir, fileName, matchingIncludes);
                    }
                    i++;
                }
                return;
            }
            matchingIncludes = new ArrayList(1);
            for (Pattern include22 : includes) {
                if (matchingIncludes.isEmpty()) {
                    matchingIncludes.add(include22);
                } else {
                    matchingIncludes.set(0, include22);
                }
                process(dir, include22.value, matchingIncludes);
            }
        }
    }

    private void process(File dir, String fileName, List<Pattern> matchingIncludes) {
        boolean isFinalMatch = false;
        List<Pattern> incrementedPatterns = new ArrayList();
        Iterator<Pattern> iter = matchingIncludes.iterator();
        while (iter.hasNext()) {
            Pattern include = (Pattern) iter.next();
            if (include.incr(fileName)) {
                incrementedPatterns.add(include);
                if (include.isExhausted()) {
                    iter.remove();
                }
            }
            if (include.wasFinalMatch()) {
                isFinalMatch = true;
            }
        }
        File file = new File(dir, fileName);
        if (isFinalMatch) {
            int length = this.rootDir.getPath().length();
            if (!this.rootDir.getPath().endsWith(File.separator)) {
                length++;
            }
            this.matches.add(file.getPath().substring(length));
        }
        if (!matchingIncludes.isEmpty() && file.isDirectory()) {
            scanDir(file, matchingIncludes);
        }
        for (Pattern include2 : incrementedPatterns) {
            include2.decr();
        }
    }

    public List<String> matches() {
        return this.matches;
    }

    public File rootDir() {
        return this.rootDir;
    }

    public static void main(String[] args) {
        List<String> includes = new ArrayList();
        includes.add("website/in*");
        List<String> excludes = new ArrayList();
        long start = System.nanoTime();
        List<String> files = new GlobScanner(new File(".."), includes, excludes, false).matches();
        long end = System.nanoTime();
        System.out.println(files.toString().replaceAll(", ", "\n").replaceAll("[\\[\\]]", ""));
        System.out.println(((float) (end - start)) / 1000000.0f);
    }
}
