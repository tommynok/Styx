package com.jamal2367.styx.adblock.util.hash

import okhttp3.internal.and

/**
 * Murmur hash 2.0.
 *
 *
 * The murmur hash is a relative fast hash function from
 * http://murmurhash.googlepages.com/ for platforms with efficient
 * multiplication.
 *
 *
 * This is a re-implementation of the original C code plus some
 * additional features.
 *
 *
 * Public domain.
 *
 * @author Viliam Holub
 * @version 1.0.2
 * @see [github.com/vlc-android](https://github.com/mstorsjo/vlc-android/blob/8887725c89e2ad9ca04490300595479fe6d170e8/vlc-android/src/org/videolan/vlc/util/MurmurHash.java)
 */
object MurmurHash {
    /**
     * Generates 32 bit hash from byte array of the given length and
     * seed.
     *
     * @param data   byte array to hash
     * @param length length of the array to hash
     * @param seed   initial seed value
     * @return 32 bit hash of the given array
     */
    /**
     * Generates 32 bit hash from byte array with default seed value.
     *
     * @param data   byte array to hash
     * @param length length of the array to hash
     * @return 32 bit hash of the given array
     */
    @JvmOverloads
    fun hash32(data: ByteArray, length: Int, seed: Int = -0x68b84d74): Int {
        // 'm' and 'r' are mixing constants generated offline.
        // They're not really 'magic', they just happen to work well.
        val m = 0x5bd1e995
        val r = 24
        // Initialize the hash to a random value
        var h = seed xor length
        val length4 = length / 4
        for (i in 0 until length4) {
            val i4 = i * 4
            var k: Int = ((data[i4 + 0] and 0xff) + (data[i4 + 1] and 0xff shl 8)
                    + (data[i4 + 2] and 0xff shl 16) + (data[i4 + 3] and 0xff shl 24))
            k *= m
            k = k xor (k ushr r)
            k *= m
            h *= m
            h = h xor k
        }
        when (length % 4) {
            3 -> {
                h = h xor (data[(length and 3.inv()) + 2] and 0xff shl 16)
                h = h xor (data[(length and 3.inv()) + 1] and 0xff shl 8)
                h = h xor (data[length and 3.inv()] and 0xff)
                h *= m
            }
            2 -> {
                h = h xor (data[(length and 3.inv()) + 1] and 0xff shl 8)
                h = h xor (data[length and 3.inv()] and 0xff)
                h *= m
            }
            1 -> {
                h = h xor (data[length and 3.inv()] and 0xff)
                h *= m
            }
        }
        h = h xor (h ushr 13)
        h *= m
        h = h xor (h ushr 15)
        return h
    }

    /**
     * Generates 32 bit hash from a string.
     *
     * @param text string to hash
     * @return 32 bit hash of the given string
     */
    fun hash32(text: String): Int {
        val bytes = text.toByteArray()
        return hash32(bytes, bytes.size)
    }

    /**
     * Generates 32 bit hash from a substring.
     *
     * @param text   string to hash
     * @param from   starting index
     * @param length length of the substring to hash
     * @return 32 bit hash of the given string
     */
    fun hash32(text: String, from: Int, length: Int): Int {
        return hash32(text.substring(from, from + length))
    }
    /**
     * Generates 64 bit hash from byte array of the given length and seed.
     *
     * @param data   byte array to hash
     * @param length length of the array to hash
     * @param seed   initial seed value
     * @return 64 bit hash of the given array
     */
    /**
     * Generates 64 bit hash from byte array with default seed value.
     *
     * @param data   byte array to hash
     * @param length length of the array to hash
     * @return 64 bit hash of the given string
     */
    @JvmOverloads
    fun hash64(data: ByteArray, length: Int, seed: Int = -0x1e85eb9b): Long {
        val m = -0x395b586ca42e166bL
        val r = 47
        var h = (seed and 0xffffffffL xor (length * m).toInt().toLong())
        val length8 = length / 8
        for (i in 0 until length8) {
            val i8 = i * 8
            var k = ((data[i8 + 0].toLong() and 0xff) + (data[i8 + 1].toLong() and 0xff shl 8)
                    + (data[i8 + 2].toLong() and 0xff shl 16) + (data[i8 + 3].toLong() and 0xff shl 24)
                    + (data[i8 + 4].toLong() and 0xff shl 32) + (data[i8 + 5].toLong() and 0xff shl 40)
                    + (data[i8 + 6].toLong() and 0xff shl 48) + (data[i8 + 7].toLong() and 0xff shl 56))
            k *= m
            k = k xor (k ushr r)
            k *= m
            h = h xor k
            h *= m
        }
        when (length % 8) {
            7 -> {
                h = h xor ((data[(length and 7.inv()) + 6] and 0xff).toLong() shl 48)
                h = h xor ((data[(length and 7.inv()) + 5] and 0xff).toLong() shl 40)
                h = h xor ((data[(length and 7.inv()) + 4] and 0xff).toLong() shl 32)
                h = h xor ((data[(length and 7.inv()) + 3] and 0xff).toLong() shl 24)
                h = h xor ((data[(length and 7.inv()) + 2] and 0xff).toLong() shl 16)
                h = h xor ((data[(length and 7.inv()) + 1] and 0xff).toLong() shl 8)
                h = h xor (data[length and 7.inv()] and 0xff).toLong()
                h *= m
            }
            6 -> {
                h = h xor ((data[(length and 7.inv()) + 5] and 0xff).toLong() shl 40)
                h = h xor ((data[(length and 7.inv()) + 4] and 0xff).toLong() shl 32)
                h = h xor ((data[(length and 7.inv()) + 3] and 0xff).toLong() shl 24)
                h = h xor ((data[(length and 7.inv()) + 2] and 0xff).toLong() shl 16)
                h = h xor ((data[(length and 7.inv()) + 1] and 0xff).toLong() shl 8)
                h = h xor (data[length and 7.inv()] and 0xff).toLong()
                h *= m
            }
            5 -> {
                h = h xor ((data[(length and 7.inv()) + 4] and 0xff).toLong() shl 32)
                h = h xor ((data[(length and 7.inv()) + 3] and 0xff).toLong() shl 24)
                h = h xor ((data[(length and 7.inv()) + 2] and 0xff).toLong() shl 16)
                h = h xor ((data[(length and 7.inv()) + 1] and 0xff).toLong() shl 8)
                h = h xor (data[length and 7.inv()] and 0xff).toLong()
                h *= m
            }
            4 -> {
                h = h xor ((data[(length and 7.inv()) + 3] and 0xff).toLong() shl 24)
                h = h xor ((data[(length and 7.inv()) + 2] and 0xff).toLong() shl 16)
                h = h xor ((data[(length and 7.inv()) + 1] and 0xff).toLong() shl 8)
                h = h xor (data[length and 7.inv()] and 0xff).toLong()
                h *= m
            }
            3 -> {
                h = h xor ((data[(length and 7.inv()) + 2] and 0xff).toLong() shl 16)
                h = h xor ((data[(length and 7.inv()) + 1] and 0xff).toLong() shl 8)
                h = h xor (data[length and 7.inv()] and 0xff).toLong()
                h *= m
            }
            2 -> {
                h = h xor ((data[(length and 7.inv()) + 1] and 0xff).toLong() shl 8)
                h = h xor (data[length and 7.inv()] and 0xff).toLong()
                h *= m
            }
            1 -> {
                h = h xor (data[length and 7.inv()] and 0xff).toLong()
                h *= m
            }
        }
        h = h xor (h ushr r)
        h *= m
        h = h xor (h ushr r)
        return h
    }

    /**
     * Generates 64 bit hash from a string.
     *
     * @param text string to hash
     * @return 64 bit hash of the given string
     */
    private fun hash64(text: String): Long {
        val bytes = text.toByteArray()
        return hash64(bytes, bytes.size)
    }

    /**
     * Generates 64 bit hash from a substring.
     *
     * @param text   string to hash
     * @param from   starting index
     * @param length length of the substring to hash
     * @return 64 bit hash of the given array
     */
    fun hash64(text: String, from: Int, length: Int): Long {
        return hash64(text.substring(from, from + length))
    }
}