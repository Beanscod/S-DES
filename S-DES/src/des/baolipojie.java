package des;
import java.util.Scanner;

public class baolipojie {

    static int[] IP = {2, 6, 3, 1, 4, 8, 5, 7};
    static int[] IPIN = {4, 1, 3, 5, 7, 2, 8, 6};
    static int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    static int[] P8 = {6, 3, 7, 4, 8, 5, 10, 9};
    static int[] EP = {4, 1, 2, 3, 2, 3, 4, 1};
    static int[] P4 = {2, 4, 3, 1};
    static int[][] SS1 = {{1, 0, 3, 2}, {3, 2, 1, 0}, {0, 2, 1, 3}, {3, 1, 0, 2}};
    static int[][] SS2 = {{0, 1, 2, 3}, {2, 3, 1, 0}, {3, 0, 1, 2}, {2, 1, 0, 3}};

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("请输入明文（8位二进制数）：");
        String plaintext = scanner.nextLine();
        System.out.print("请输入密文（8位二进制数）：");
        String ciphertext = scanner.nextLine();
        scanner.close();

        long startTime = System.nanoTime();
        long totalDelay = bruteForceKey(plaintext, ciphertext);
        long endTime = System.nanoTime();

        long duration = endTime - startTime;
        System.out.println("破解时间: " + duration / 1_000_000 + " 毫秒");
        System.out.println("总延时: " + totalDelay + " 毫秒");
    }

    public static long bruteForceKey(String plaintext, String ciphertext) {
        int totalKeys = 1024; // 10位密钥的总数
        long totalDelay = 0; // 总延时
        for (int i = 0; i < totalKeys; i++) {
            String key = String.format("%10s", Integer.toBinaryString(i)).replace(' ', '0');
            String generatedCiphertext = encrypt(plaintext, key);
            if (generatedCiphertext.equals(ciphertext)) {
                System.out.println("找到匹配的密钥: " + key);
            }

            // 动态进度条
            if (i % (totalKeys / 20) == 0) { // 每5%更新一次
                long delay = printAnimatedProgress(i, totalKeys);
                totalDelay += delay; // 累加延时
            }
        }
        System.out.println(); // 换行
        return totalDelay; // 返回总延时
    }

    public static long printAnimatedProgress(int current, int total) {
        int progressBarWidth = 50; // 进度条宽度
        int progress = (int) ((current / (double) total) * progressBarWidth);
        StringBuilder bar = new StringBuilder("[");

        for (int j = 0; j < progressBarWidth; j++) {
            if (j < progress) {
                bar.append("=");
            } else {
                bar.append(" ");
            }
        }
        bar.append("] ").append((current * 100 / total)).append("%");

        // 清除上一行，打印当前进度
        System.out.print("\r" + bar.toString());

        // 动画效果
        long startDelay = System.currentTimeMillis();
        try {
            Thread.sleep(100); // 每次更新之间的延时
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        long endDelay = System.currentTimeMillis();
        return endDelay - startDelay; // 返回延时
    }

    public static String encrypt(String plaintext, String key) {
        // 初始置换
        plaintext = replace(plaintext, IP, 8);
        String L0 = plaintext.substring(0, 4);
        String R0 = plaintext.substring(4);
        String L1 = R0;

        // 生成 K1 和 K2
        key = replace(key, P10, 10);
        String LS1 = leftMove(key, 1);
        String LS2 = leftMove(LS1, 2);
        String K1 = replace(LS1, P8, 8);
        String K2 = replace(LS2, P8, 8);

        // 第一次 F 运算
        String F1 = RandKtoS(R0, K1);
        String R1 = xorOperation(F1, L0);
        L1 = R0;

        // 第二次 F 运算
        String F2 = RandKtoS(R1, K2);
        String L2 = xorOperation(L1, F2);
        String R2 = R1;

        String result = L2 + R2;

        // 逆初始置换
        return replace(result, IPIN, 8);
    }

    public static String replace(String str, int[] arr, int len) {
        char[] item = new char[len];
        for (int i = 0; i < len; i++) {
            int index = arr[i] - 1;
            item[i] = str.charAt(index);
        }
        return new String(item);
    }

    public static String leftMove(String str, int times) {
        int len = str.length();
        int mid = len / 2;
        String leftPart = str.substring(0, mid);
        String rightPart = str.substring(mid);
        String leftMoved = leftPart.substring(times % mid) + leftPart.substring(0, times % mid);
        String rightMoved = rightPart.substring(times % (len - mid)) + rightPart.substring(0, times % (len - mid));
        return leftMoved + rightMoved;
    }

    public static String xorOperation(String str1, String str2) {
        int len = str1.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            result.append(str1.charAt(i) == str2.charAt(i) ? '0' : '1');
        }
        return result.toString();
    }

    public static String RandKtoS(String R, String K) {
        R = replace(R, EP, 8);
        String XOR = xorOperation(R, K);

        String S1 = XOR.substring(0, 4);
        String S2 = XOR.substring(4);

        int S1H = binaryToDecimal(S1.substring(0, 2));
        int S1L = binaryToDecimal(S1.substring(2));
        int S2H = binaryToDecimal(S2.substring(0, 2));
        int S2L = binaryToDecimal(S2.substring(2));

        int SL = SS1[S1H][S1L];
        int SR = SS2[S2H][S2L];

        String SL2 = String.format("%2s", Integer.toBinaryString(SL)).replace(' ', '0');
        String SR2 = String.format("%2s", Integer.toBinaryString(SR)).replace(' ', '0');
        String S = SL2 + SR2;

        return replace(S, P4, 4);
    }

    public static int binaryToDecimal(String binary) {
        return Integer.parseInt(binary, 2);
    }
}