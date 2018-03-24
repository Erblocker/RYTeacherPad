package com.netspace.library.struct;

import java.nio.ByteBuffer;
import java.util.zip.CRC32;

public class UDPDataSendCache {
    private final int CACHESIZE = 2048;
    private UDPCacheItem[] marrCache = new UDPCacheItem[2048];
    private int mnCacheIndex = 0;

    public interface UDPCacheFoundCallBack {
        void onCacheFound(byte[] bArr, int i, int i2, int i3);
    }

    public static class UDPCacheItem {
        public ByteBuffer content = ByteBuffer.allocate(1210);
        public int nLength;
        public int nPackageIndex;
        public int nStartPos;
    }

    public UDPDataSendCache() {
        for (int i = 0; i < 2048; i++) {
            this.marrCache[i] = new UDPCacheItem();
        }
    }

    public void writeToCache(byte[] data, int nLength, int nPackageIndex, int nStartPos) {
        UDPCacheItem cacheItem = this.marrCache[this.mnCacheIndex];
        cacheItem.content.clear();
        cacheItem.content.put(data, 0, nLength);
        cacheItem.content.put(1, (byte) 2);
        CRC32 crc = new CRC32();
        crc.update(cacheItem.content.array(), 0, nLength - 2);
        cacheItem.content.put(nLength - 1, (byte) ((int) crc.getValue()));
        cacheItem.nLength = nLength;
        cacheItem.nPackageIndex = nPackageIndex;
        cacheItem.nStartPos = nStartPos;
        this.mnCacheIndex++;
        if (this.mnCacheIndex >= 2048) {
            this.mnCacheIndex = 0;
        }
    }

    public UDPCacheItem findInCache(int nPackageIndex, int nStartPos, UDPCacheFoundCallBack CallBack) {
        int i = 0;
        while (i < this.marrCache.length) {
            UDPCacheItem cacheItem = this.marrCache[i];
            if (cacheItem.nPackageIndex != nPackageIndex || cacheItem.nStartPos != nStartPos) {
                i++;
            } else if (CallBack == null) {
                return cacheItem;
            } else {
                CallBack.onCacheFound(cacheItem.content.array(), cacheItem.nLength, cacheItem.nPackageIndex, cacheItem.nStartPos);
                return cacheItem;
            }
        }
        return null;
    }
}
