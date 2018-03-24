package com.esotericsoftware.wildcard;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

class RegexScanner {
    private final List<Pattern> includePatterns;
    private final List<String> matches = new ArrayList(128);
    private final File rootDir;

    public RegexScanner(File rootDir, List<String> includes, List<String> excludes) {
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
                    this.includePatterns = new ArrayList();
                    for (String include : includes) {
                        this.includePatterns.add(Pattern.compile(include, 2));
                    }
                    List<Pattern> excludePatterns = new ArrayList();
                    for (String exclude : excludes) {
                        excludePatterns.add(Pattern.compile(exclude, 2));
                    }
                    scanDir(rootDir);
                    Iterator<String> matchIter = this.matches.iterator();
                    while (matchIter.hasNext()) {
                        String filePath = (String) matchIter.next();
                        for (Pattern exclude2 : excludePatterns) {
                            if (exclude2.matcher(filePath).matches()) {
                                matchIter.remove();
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

    private void scanDir(File dir) {
        for (File file : dir.listFiles()) {
            for (Pattern include : this.includePatterns) {
                int length = this.rootDir.getPath().length();
                if (!this.rootDir.getPath().endsWith(File.separator)) {
                    length++;
                }
                String filePath = file.getPath().substring(length);
                if (include.matcher(filePath).matches()) {
                    this.matches.add(filePath);
                    break;
                }
            }
            if (file.isDirectory()) {
                scanDir(file);
            }
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
        includes.add("core[^T]+php");
        List<String> excludes = new ArrayList();
        long start = System.nanoTime();
        List<String> files = new RegexScanner(new File("..\\website\\includes"), includes, excludes).matches();
        long end = System.nanoTime();
        System.out.println(files.toString().replaceAll(", ", "\n").replaceAll("[\\[\\]]", ""));
        System.out.println(((float) (end - start)) / 1000000.0f);
    }
}
