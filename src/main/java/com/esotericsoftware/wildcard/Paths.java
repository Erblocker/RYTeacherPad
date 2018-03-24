package com.esotericsoftware.wildcard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Paths implements Iterable<String> {
    private static final Comparator<Path> LONGEST_TO_SHORTEST = new Comparator<Path>() {
        public int compare(Path s1, Path s2) {
            return s2.absolute().length() - s1.absolute().length();
        }
    };
    private static List<String> defaultGlobExcludes;
    final HashSet<Path> paths = new HashSet(32);

    public static final class Path {
        public final String dir;
        public final String name;

        public Path(String dir, String name) {
            if (dir.length() > 0 && !dir.endsWith("/")) {
                dir = new StringBuilder(String.valueOf(dir)).append("/").toString();
            }
            this.dir = dir;
            this.name = name;
        }

        public String absolute() {
            return this.dir + this.name;
        }

        public File file() {
            return new File(this.dir, this.name);
        }

        public int hashCode() {
            int i = 0;
            int hashCode = ((this.dir == null ? 0 : this.dir.hashCode()) + 31) * 31;
            if (this.name != null) {
                i = this.name.hashCode();
            }
            return hashCode + i;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Path other = (Path) obj;
            if (this.dir == null) {
                if (other.dir != null) {
                    return false;
                }
            } else if (!this.dir.equals(other.dir)) {
                return false;
            }
            if (this.name == null) {
                if (other.name != null) {
                    return false;
                }
                return true;
            } else if (this.name.equals(other.name)) {
                return true;
            } else {
                return false;
            }
        }
    }

    public Paths(String dir, String... patterns) {
        glob(dir, patterns);
    }

    public Paths(String dir, List<String> patterns) {
        glob(dir, (List) patterns);
    }

    private Paths glob(String dir, boolean ignoreCase, String... patterns) {
        if (dir == null) {
            dir = ".";
        }
        if (patterns != null && patterns.length == 0) {
            String[] split = dir.split("\\|");
            if (split.length > 1) {
                dir = split[0];
                patterns = new String[(split.length - 1)];
                int n = split.length;
                for (int i = 1; i < n; i++) {
                    patterns[i - 1] = split[i];
                }
            }
        }
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            List<String> includes = new ArrayList();
            List<String> excludes = new ArrayList();
            if (patterns != null) {
                for (String pattern : patterns) {
                    if (pattern.charAt(0) == '!') {
                        excludes.add(pattern.substring(1));
                    } else {
                        includes.add(pattern);
                    }
                }
            }
            if (includes.isEmpty()) {
                includes.add("**");
            }
            if (defaultGlobExcludes != null) {
                excludes.addAll(defaultGlobExcludes);
            }
            GlobScanner scanner = new GlobScanner(dirFile, includes, excludes, ignoreCase);
            String rootDir = scanner.rootDir().getPath().replace('\\', '/');
            if (!rootDir.endsWith("/")) {
                rootDir = new StringBuilder(String.valueOf(rootDir)).append('/').toString();
            }
            for (String filePath : scanner.matches()) {
                this.paths.add(new Path(rootDir, filePath));
            }
        }
        return this;
    }

    public Paths glob(String dir, String... patterns) {
        return glob(dir, false, patterns);
    }

    public Paths globIgnoreCase(String dir, String... patterns) {
        return glob(dir, true, patterns);
    }

    public Paths glob(String dir, List<String> patterns) {
        if (patterns == null) {
            throw new IllegalArgumentException("patterns cannot be null.");
        }
        glob(dir, false, (String[]) patterns.toArray(new String[patterns.size()]));
        return this;
    }

    public Paths globIgnoreCase(String dir, List<String> patterns) {
        if (patterns == null) {
            throw new IllegalArgumentException("patterns cannot be null.");
        }
        glob(dir, true, (String[]) patterns.toArray(new String[patterns.size()]));
        return this;
    }

    public Paths regex(String dir, String... patterns) {
        if (dir == null) {
            dir = ".";
        }
        if (patterns != null && patterns.length == 0) {
            String[] split = dir.split("\\|");
            if (split.length > 1) {
                dir = split[0];
                patterns = new String[(split.length - 1)];
                int n = split.length;
                for (int i = 1; i < n; i++) {
                    patterns[i - 1] = split[i];
                }
            }
        }
        File dirFile = new File(dir);
        if (dirFile.exists()) {
            List<String> includes = new ArrayList();
            List<String> excludes = new ArrayList();
            if (patterns != null) {
                for (String pattern : patterns) {
                    if (pattern.charAt(0) == '!') {
                        excludes.add(pattern.substring(1));
                    } else {
                        includes.add(pattern);
                    }
                }
            }
            if (includes.isEmpty()) {
                includes.add(".*");
            }
            RegexScanner scanner = new RegexScanner(dirFile, includes, excludes);
            String rootDir = scanner.rootDir().getPath().replace('\\', '/');
            if (!rootDir.endsWith("/")) {
                rootDir = new StringBuilder(String.valueOf(rootDir)).append('/').toString();
            }
            for (String filePath : scanner.matches()) {
                this.paths.add(new Path(rootDir, filePath));
            }
        }
        return this;
    }

    public Paths copyTo(String destDir) throws IOException {
        Paths newPaths = new Paths();
        Iterator it = this.paths.iterator();
        while (it.hasNext()) {
            Path path = (Path) it.next();
            File destFile = new File(destDir, path.name);
            File srcFile = path.file();
            if (srcFile.isDirectory()) {
                destFile.mkdirs();
            } else {
                destFile.getParentFile().mkdirs();
                copyFile(srcFile, destFile);
            }
            newPaths.paths.add(new Path(destDir, path.name));
        }
        return newPaths;
    }

    public boolean delete() {
        boolean success = true;
        List<Path> pathsCopy = new ArrayList(this.paths);
        Collections.sort(pathsCopy, LONGEST_TO_SHORTEST);
        Iterator it = getFiles(pathsCopy).iterator();
        while (it.hasNext()) {
            File file = (File) it.next();
            if (file.isDirectory()) {
                if (!deleteDirectory(file)) {
                    success = false;
                }
            } else if (!file.delete()) {
                success = false;
            }
        }
        return success;
    }

    public void zip(String destFile) throws IOException {
        Paths zipPaths = filesOnly();
        if (!zipPaths.paths.isEmpty()) {
            byte[] buf = new byte[1024];
            ZipOutputStream out = new ZipOutputStream(new FileOutputStream(destFile));
            out.setLevel(9);
            Iterator it = zipPaths.paths.iterator();
            while (it.hasNext()) {
                try {
                    Path path = (Path) it.next();
                    File file = path.file();
                    out.putNextEntry(new ZipEntry(path.name.replace('\\', '/')));
                    FileInputStream in = new FileInputStream(file);
                    while (true) {
                        int len = in.read(buf);
                        if (len <= 0) {
                            break;
                        }
                        out.write(buf, 0, len);
                    }
                    in.close();
                    out.closeEntry();
                } finally {
                    out.close();
                }
            }
        }
    }

    public int count() {
        return this.paths.size();
    }

    public boolean isEmpty() {
        return this.paths.isEmpty();
    }

    public String toString(String delimiter) {
        StringBuffer buffer = new StringBuffer(256);
        for (String path : getPaths()) {
            if (buffer.length() > 0) {
                buffer.append(delimiter);
            }
            buffer.append(path);
        }
        return buffer.toString();
    }

    public String toString() {
        return toString(", ");
    }

    public Paths flatten() {
        Paths newPaths = new Paths();
        Iterator it = this.paths.iterator();
        while (it.hasNext()) {
            File file = ((Path) it.next()).file();
            if (file.isFile()) {
                newPaths.paths.add(new Path(file.getParent(), file.getName()));
            }
        }
        return newPaths;
    }

    public Paths filesOnly() {
        Paths newPaths = new Paths();
        Iterator it = this.paths.iterator();
        while (it.hasNext()) {
            Path path = (Path) it.next();
            if (path.file().isFile()) {
                newPaths.paths.add(path);
            }
        }
        return newPaths;
    }

    public Paths dirsOnly() {
        Paths newPaths = new Paths();
        Iterator it = this.paths.iterator();
        while (it.hasNext()) {
            Path path = (Path) it.next();
            if (path.file().isDirectory()) {
                newPaths.paths.add(path);
            }
        }
        return newPaths;
    }

    public List<File> getFiles() {
        return getFiles(new ArrayList(this.paths));
    }

    private ArrayList<File> getFiles(List<Path> paths) {
        ArrayList<File> files = new ArrayList(paths.size());
        for (Path path : paths) {
            files.add(path.file());
        }
        return files;
    }

    public List<String> getRelativePaths() {
        ArrayList<String> stringPaths = new ArrayList(this.paths.size());
        Iterator it = this.paths.iterator();
        while (it.hasNext()) {
            stringPaths.add(((Path) it.next()).name);
        }
        return stringPaths;
    }

    public List<String> getPaths() {
        ArrayList<String> stringPaths = new ArrayList(this.paths.size());
        for (File file : getFiles()) {
            stringPaths.add(file.getPath());
        }
        return stringPaths;
    }

    public List<String> getNames() {
        ArrayList<String> stringPaths = new ArrayList(this.paths.size());
        for (File file : getFiles()) {
            stringPaths.add(file.getName());
        }
        return stringPaths;
    }

    public Paths addFile(String fullPath) {
        File file = new File(fullPath);
        String parent = file.getParent();
        HashSet hashSet = this.paths;
        if (parent == null) {
            parent = "";
        }
        hashSet.add(new Path(parent, file.getName()));
        return this;
    }

    public Paths add(String dir, String name) {
        this.paths.add(new Path(dir, name));
        return this;
    }

    public void add(Paths paths) {
        this.paths.addAll(paths.paths);
    }

    public Iterator<String> iterator() {
        return new Iterator<String>() {
            private Iterator<Path> iter;

            {
                this.iter = Paths.this.paths.iterator();
            }

            public void remove() {
                this.iter.remove();
            }

            public String next() {
                return ((Path) this.iter.next()).absolute();
            }

            public boolean hasNext() {
                return this.iter.hasNext();
            }
        };
    }

    public Iterator<File> fileIterator() {
        return new Iterator<File>() {
            private Iterator<Path> iter;

            {
                this.iter = Paths.this.paths.iterator();
            }

            public void remove() {
                this.iter.remove();
            }

            public File next() {
                return ((Path) this.iter.next()).file();
            }

            public boolean hasNext() {
                return this.iter.hasNext();
            }
        };
    }

    public static void setDefaultGlobExcludes(String... defaultGlobExcludes) {
        defaultGlobExcludes = Arrays.asList(defaultGlobExcludes);
    }

    private static void copyFile(File in, File out) throws IOException {
        FileInputStream sourceStream = new FileInputStream(in);
        FileOutputStream destinationStream = new FileOutputStream(out);
        FileChannel sourceChannel = sourceStream.getChannel();
        FileChannel destinationChannel = destinationStream.getChannel();
        sourceChannel.transferTo(0, sourceChannel.size(), destinationChannel);
        sourceChannel.close();
        sourceStream.close();
        destinationChannel.close();
        destinationStream.close();
    }

    private static boolean deleteDirectory(File file) {
        if (file.exists()) {
            File[] files = file.listFiles();
            int n = files.length;
            for (int i = 0; i < n; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return file.delete();
    }
}
