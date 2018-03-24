package com.netspace.library.virtualnetworkobject;

import com.netspace.library.struct.CachePackage;
import com.netspace.library.utilities.Utilities;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;

public class ItemCache {
    private String mCachePath;

    public ItemCache(String szCachePath) {
        this.mCachePath = szCachePath;
        if (!this.mCachePath.endsWith("/")) {
            this.mCachePath += "/";
        }
    }

    protected String getItemObjectHashKey(ItemObject ItemObject) {
        return Utilities.md5(ItemObject.toString());
    }

    public boolean writeToCache(ItemObject ItemObject) {
        String szHashKey = getItemObjectHashKey(ItemObject);
        CachePackage CachePackage = new CachePackage();
        if (ItemObject.readTextData() == null) {
            return false;
        }
        CachePackage.szPackageContent = ItemObject.readTextData();
        CachePackage.nTimeoutInMS = ItemObject.mExpireTimeInMS;
        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File(this.mCachePath + szHashKey + ".cache")));
            oos.writeObject(CachePackage);
            oos.flush();
            oos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e2) {
            e2.printStackTrace();
            return false;
        }
    }

    public boolean readFromCache(ItemObject ItemObject) {
        StreamCorruptedException e;
        IOException e2;
        File TargetFile = new File(this.mCachePath + getItemObjectHashKey(ItemObject) + ".cache");
        if (TargetFile.exists()) {
            InputStream inputStream = null;
            boolean bDeleteFile = false;
            try {
                InputStream inputStream2 = new FileInputStream(TargetFile);
                try {
                    Object o = new ObjectInputStream(inputStream2).readObject();
                    if (o instanceof CachePackage) {
                        CachePackage CachePackage = (CachePackage) o;
                        if (CachePackage.nTimeoutInMS == 0 || VirtualNetworkObject.getOfflineMode()) {
                            ItemObject.writeTextData(CachePackage.szPackageContent);
                            return true;
                        } else if (System.currentTimeMillis() > CachePackage.nTimeoutInMS) {
                            TargetFile.delete();
                            return false;
                        }
                    }
                    inputStream = inputStream2;
                } catch (StreamCorruptedException e3) {
                    e = e3;
                    inputStream = inputStream2;
                    bDeleteFile = true;
                    e.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    if (bDeleteFile) {
                        TargetFile.delete();
                    }
                    return false;
                } catch (FileNotFoundException e4) {
                    e = e4;
                    inputStream = inputStream2;
                    e.printStackTrace();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bDeleteFile) {
                        TargetFile.delete();
                    }
                    return false;
                } catch (IOException e5) {
                    e22 = e5;
                    inputStream = inputStream2;
                    bDeleteFile = true;
                    e22.printStackTrace();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bDeleteFile) {
                        TargetFile.delete();
                    }
                    return false;
                } catch (ClassNotFoundException e6) {
                    e = e6;
                    inputStream = inputStream2;
                    bDeleteFile = true;
                    e.printStackTrace();
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bDeleteFile) {
                        TargetFile.delete();
                    }
                    return false;
                }
            } catch (StreamCorruptedException e7) {
                e = e7;
                bDeleteFile = true;
                e.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bDeleteFile) {
                    TargetFile.delete();
                }
                return false;
            } catch (FileNotFoundException e8) {
                FileNotFoundException e9;
                e9 = e8;
                e9.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bDeleteFile) {
                    TargetFile.delete();
                }
                return false;
            } catch (IOException e10) {
                e22 = e10;
                bDeleteFile = true;
                e22.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bDeleteFile) {
                    TargetFile.delete();
                }
                return false;
            } catch (ClassNotFoundException e11) {
                ClassNotFoundException e12;
                e12 = e11;
                bDeleteFile = true;
                e12.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
                if (bDeleteFile) {
                    TargetFile.delete();
                }
                return false;
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (bDeleteFile) {
                TargetFile.delete();
            }
        }
        return false;
    }

    public boolean hasCache(ItemObject ItemObject) {
        return new File(this.mCachePath + getItemObjectHashKey(ItemObject) + ".cache").exists();
    }
}
