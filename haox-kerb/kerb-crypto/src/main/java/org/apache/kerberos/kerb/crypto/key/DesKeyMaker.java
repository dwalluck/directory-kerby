package org.apache.kerberos.kerb.crypto.key;

import org.apache.kerberos.kerb.KrbException;
import org.apache.kerberos.kerb.crypto.BytesUtil;
import org.apache.kerberos.kerb.crypto.Des;
import org.apache.kerberos.kerb.crypto.enc.EncryptProvider;

public class DesKeyMaker extends AbstractKeyMaker {

    public DesKeyMaker(EncryptProvider encProvider) {
        super(encProvider);
    }

    @Override
    public byte[] str2key(String string, String salt, byte[] param) throws KrbException {
        String error = null;
        int type = 0;

        if (param != null) {
            if (param.length != 1) {
                error = "Invalid param to S2K";
            }
            type = param[0];
            if (type != 0 && type != 1) {
                error = "Invalid param to S2K";
            }
        }
        if (type == 1) {
            error = "AFS not supported yet";
        }

        if (error != null) {
            throw new KrbException(error);
        }

        char[] passwdSalt = makePasswdSalt(string, salt);
        byte[] key = toKey(passwdSalt);
        return key;
    }

    /**
     mit_des_string_to_key(string,salt) {
       odd = 1;
       s = string | salt;
       tempstring = 0; // 56-bit string
       pad(s); // with nulls to 8 byte boundary
       for (8byteblock in s) {
         56bitstring = removeMSBits(8byteblock);
         if (odd == 0) reverse(56bitstring);
         odd = ! odd;
         tempstring = tempstring XOR 56bitstring;
       }
       tempkey = key_correction(add_parity_bits(tempstring));
       key = key_correction(DES-CBC-check(s,tempkey));
       return(key);
     }
     */
    private byte[] toKey(char[] passwdChars) throws KrbException {
        int keySize = encProvider().keySize();

        byte[] bytes = (new String(passwdChars)).getBytes();

        // padded with zero-valued octets to a multiple of eight octets.
        byte[] paddedBytes = BytesUtil.padding(bytes, keySize);

        int blocksOfbytes8 = paddedBytes.length / keySize;
        boolean odd = true;
        byte[] bits56 = new byte[8];
        byte[] tempString = new byte[8];
        for (int i = 0; i < blocksOfbytes8; ++i) {
            System.arraycopy(paddedBytes, 8 * i, bits56, 0, 8);
            removeMSBits(bits56);
            if (odd) {
                reverse(bits56);
            }
            odd = ! odd;
            BytesUtil.xor(bits56, 0, tempString);
        }

        byte[] keyBytes = addParityBits(tempString);
        keyCorrection(keyBytes);

        byte[] resultKey = null;
        if (encProvider().supportCbcMac()) {
            resultKey = encProvider().cbcMac(keyBytes, keyBytes, paddedBytes);
        } else {
            throw new KrbException("cbcMac should be supported by the provider: "
                    + encProvider().getClass());
        }

        keyCorrection(resultKey);

        return resultKey;
    }

    /**
     * Note this isn't hit any test yet, and very probably problematic
     */
    @Override
    public byte[] random2Key(byte[] randomBits) throws KrbException {
        if (randomBits.length != encProvider().keyInputSize()) {
            throw new KrbException("Invalid random bits, not of correct bytes size");
        }

        /**
         * Ref. k5_rand2key_des in random_to_key.c in MIT krb5
         * Take the seven bytes, move them around into the top 7 bits of the
         * 8 key bytes, then compute the parity bits.  Do this three times.
         */
        byte[] key = new byte[encProvider().keySize()];
        int tmp;
        System.arraycopy(randomBits, 0, key, 0, 7);

        key[7] = (byte) (((key[0] & 1) << 1) |
                ((key[1] & 1) << 2) |
                ((key[2] & 1) << 3) |
                ((key[3] & 1) << 4) |
                ((key[4] & 1) << 5) |
                ((key[5] & 1) << 6) |
                ((key[6] & 1) << 7));

        for (int i = 0; i < 8; i++) {
            tmp = key[i] & 0xfe;
            tmp |= (Integer.bitCount(tmp) & 1) ^ 1;
            key[i] = (byte) tmp;
        }

        Des.fixKey(key, 0, 8);

        return key;
    }

    // Processing an 8bytesblock
    private static byte[] removeMSBits(byte[] bits56) {
        /**
         Treats a 64 bit block as 8 octets and removes the MSB in
         each octet (in big endian mode) and concatenates the result.
         E.g., the input octet string:
         01110000 01100001 11110011  01110011 11110111 01101111 11110010 01100100
         =>
         1110000 1100001 1110011  1110011 1110111 1101111 1110010 1100100
         */

        /**
         * We probably do nothing here, just pretending the MSB bit to be discarded,
         * and ensure the MSB will not be used in the following processing.
         */

        return bits56;
    }

    // Processing an 56bitblock
    private static void reverse(byte[] bits56) {
        /**
         Treats a 56-bit block as a binary string and reverses it.
         E.g., the input string:
         1000001 1010100 1001000 1000101 1001110 1000001 0101110 1001101
         =>
         1000001 0010101 0001001 1010001 0111001 1000001 0101110 1011001
         =>
         1011001 0111010 1000001  0111001 1010001 0001001 0010101 1000001
         */

        // Reversing in a 7bit
        int t1, t2;
        byte bt;
        for (int i = 0; i < 8; ++i) {
            bt = bits56[i];

            t1 = (bt >> 6) & 1;
            t2 = (bt >> 0) & 1;
            if (t1 != t2) bt ^= (1 << 6 | 1 << 0);

            t1 = (bt >> 5) & 1;
            t2 = (bt >> 1) & 1;
            if (t1 != t2) bt ^= (1 << 5 | 1 << 1);

            t1 = (bt >> 4) & 1;
            t2 = (bt >> 2) & 1;
            if (t1 != t2) bt ^= (1 << 4 | 1 << 2);

            bits56[i] = bt;
        }

        // Reversing the 8 7bit
        bt = bits56[7];
        bits56[7] = bits56[0];
        bits56[0] = bt;

        bt = bits56[6];
        bits56[6] = bits56[1];
        bits56[1] = bt;

        bt = bits56[5];
        bits56[5] = bits56[2];
        bits56[2] = bt;

        bt = bits56[4];
        bits56[4] = bits56[3];
        bits56[3] = bt;
    }

    private static byte[] addParityBits(byte[] bits56) {
        /**
         Copies a 56-bit block into a 64-bit block, left shifts
         content in each octet, and add DES parity bit.
         E.g., the input string:
         1100000 0001111 0011100  0110100 1000101 1100100 0110110 0010111
         =>
         11000001 00011111 00111000  01101000 10001010 11001000 01101101 00101111
         */
        byte bt;
        for (int i = 0; i < 8; i++) {
            bits56[i] <<= 1;
        }
        addParity(bits56);

        return bits56;
    }

    private static void keyCorrection(byte[] key) {
        addParity(key);
        if (Des.isWeakKey(key, 0, key.length)) {
            Des.fixKey(key, 0, key.length);
        }
    }

    private static int smask(int step) {
        return (1 << step) - 1;
    }

    private static byte pstep(byte x, int step) {
        return (byte) ((x & smask(step)) ^ ((x >> step) & smask(step)));
    }

    private static byte parityChar(byte abyte) {
        //#define smask(step) ((1<<step)-1)
        //#define pstep(x,step) (((x)&smask(step))^(((x)>>step)&smask(step)))
        //#define parity_char(x) pstep(pstep(pstep((x),4),2),1)
        return pstep(pstep(pstep(abyte, 4), 2), 1);
    }

    private static void addParity(byte[] key) {
        for (int i = 0; i < key.length; ++i) {
            key[i] &= 0xfe;
            key[i] |= 1 ^ parityChar(key[i]);
        }
    }

    // Returns true if the key has correct des parity
    private static boolean checkKeyParity(byte[] key) {
        for (int i = 0; i < key.length; ++i) {
            if ((key[i] & 1) == parityChar((byte) (key[i] & 0xfe))) {
                return false;
            }
        }
        return true;
    }
}
