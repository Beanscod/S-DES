package des;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class desmethod {
    // S-DES 置换和S盒定义
    private static final int[] IP = {2, 6, 3, 1, 4, 8, 5, 7};
    private static final int[] IPIN = {4, 1, 3, 5, 7, 2, 8, 6};
    private static final int[] P10 = {3, 5, 2, 7, 4, 10, 1, 9, 8, 6};
    private static final int[] P8 = {6, 3, 7, 4, 8, 5, 10, 9};
    private static final int[] EP = {4, 1, 2, 3, 2, 3, 4, 1};
    private static final int[] P4 = {2, 4, 3, 1};
    private static final int[][] SS1 = {
            {1, 0, 3, 2},
            {3, 2, 1, 0},
            {0, 2, 1, 3},
            {3, 1, 0, 2}
    };
    private static final int[][] SS2 = {
            {0, 1, 2, 3},
            {2, 3, 1, 0},
            {3, 0, 1, 2},
            {2, 1, 0, 3}
    };

    public static void main(String[] args) {
        // 创建主窗口
        JFrame frame = new JFrame("S-DES 加密解密工具");
        frame.setSize(600, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();

        // 输入模式选择
        JLabel inputModeLabel = new JLabel("请选择输入模式：");
        String[] modes = {"ASCII", "8位二进制"};
        JComboBox<String> inputModeComboBox = new JComboBox<>(modes);

        // 明文/密文输入
        JLabel inputLabel = new JLabel("请输入明文/密文：");
        JTextField inputField = new JTextField();

        // 密钥输入
        JLabel keyLabel = new JLabel("请输入密钥（10位二进制）：");
        JTextField keyField = new JTextField();

        // 操作按钮
        JButton encryptButton = new JButton("加密");
        JButton decryptButton = new JButton("解密");

        // 结果显示
        JLabel outputLabel = new JLabel("输出结果：");
        JTextField outputField = new JTextField();
        outputField.setEditable(false);

        // 错误提示
        JLabel errorLabel = new JLabel("");
        errorLabel.setForeground(Color.RED);

        // 优化布局：使用通用方法减少重复
        gbc.insets = new Insets(10, 10, 10, 10);  // 组件之间的间距
        gbc.fill = GridBagConstraints.HORIZONTAL; // 水平填充
        gbc.weightx = 1.0;  // 水平方向扩展
        gbc.weighty = 1.0;  // 垂直方向扩展

        addComponent(frame, inputModeLabel, 0, 0, gbc);
        addComponent(frame, inputModeComboBox, 1, 0, gbc);

        addComponent(frame, inputLabel, 0, 1, gbc);
        addComponent(frame, inputField, 1, 1, gbc);

        addComponent(frame, keyLabel, 0, 2, gbc);
        addComponent(frame, keyField, 1, 2, gbc);

        // 调整按钮的布局
        gbc.fill = GridBagConstraints.NONE; // 恢复默认填充模式
        gbc.anchor = GridBagConstraints.CENTER; // 居中显示按钮
        gbc.weightx = 0.5;  // 水平方向等分
        gbc.weighty = 0.5;  // 垂直方向等分

        addComponent(frame, encryptButton, 0, 3, gbc);
        addComponent(frame, decryptButton, 1, 3, gbc);

        // 恢复默认设置并添加剩余组件
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;

        addComponent(frame, errorLabel, 0, 4, gbc);
        addComponent(frame, outputLabel, 0, 5, gbc);
        addComponent(frame, outputField, 1, 5, gbc);


        // 初始状态：如果选择ASCII模式，则解密按钮不可用
        inputModeComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String selectedMode = (String) inputModeComboBox.getSelectedItem();
                if (selectedMode.equals("ASCII")) {
                    decryptButton.setEnabled(false);
                } else {
                    decryptButton.setEnabled(true);
                }
                // 清空之前的输入和输出
                inputField.setText("");
                keyField.setText("");
                outputField.setText("");
                errorLabel.setText("");
            }
        });

        // 加密按钮逻辑
        encryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputText = inputField.getText().trim();
                String key = keyField.getText().trim();
                String inputMode = (String) inputModeComboBox.getSelectedItem();

                // 清除之前的错误提示
                errorLabel.setText("");

                // 检查密钥是否是10位二进制数
                if (!key.matches("[01]{10}")) {
                    errorLabel.setText("密钥必须是10位二进制数！");
                    return;
                }

                String plaintext = "";
                if (inputMode.equals("ASCII")) {
                    if (inputText.isEmpty()) {
                        errorLabel.setText("ASCII输入不能为空！");
                        return;
                    }
                    // ASCII 模式转换为二进制
                    plaintext = asciiToBinary(inputText);
                } else if (inputMode.equals("8位二进制")) {
                    if (!inputText.matches("([01]{8})+")) {
                        errorLabel.setText("二进制输入必须是8位的倍数！");
                        return;
                    }
                    plaintext = inputText;
                }

                // 生成 K1 和 K2
                String K1, K2;
                try {
                    String processedKey = replace(key, P10, 10);
                    String LS1 = leftMove(processedKey, 1);
                    String LS2 = leftMove(LS1, 2);
                    K1 = replace(LS1, P8, 8);
                    K2 = replace(LS2, P8, 8);
                } catch (Exception ex) {
                    errorLabel.setText("密钥处理错误！");
                    return;
                }

                // 进行加密
                String ciphertext = encryptAsciiInput(plaintext, K1, K2);

                // 根据模式输出结果
                String result = "";
                if (inputMode.equals("ASCII")) {
                    result = binaryToAscii(ciphertext);
                } else {
                    result = ciphertext;
                }

                outputField.setText(result);
            }
        });

        // 解密按钮逻辑
        decryptButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputText = inputField.getText().trim();
                String key = keyField.getText().trim();
                String inputMode = (String) inputModeComboBox.getSelectedItem();

                // 清除之前的错误提示
                errorLabel.setText("");

                // 检查密钥是否是10位二进制数
                if (!key.matches("[01]{10}")) {
                    errorLabel.setText("密钥必须是10位二进制数！");
                    return;
                }

                String ciphertext = "";
                if (inputMode.equals("8位二进制")) {
                    if (!inputText.matches("([01]{8})+")) {
                        errorLabel.setText("二进制输入必须是8位的倍数！");
                        return;
                    }
                    ciphertext = inputText;
                } else {
                    errorLabel.setText("ASCII模式下无法解密！");
                    return;
                }

                // 生成 K1 和 K2
                String K1, K2;
                try {
                    String processedKey = replace(key, P10, 10);
                    String LS1 = leftMove(processedKey, 1);
                    String LS2 = leftMove(LS1, 2);
                    K1 = replace(LS1, P8, 8);
                    K2 = replace(LS2, P8, 8);
                } catch (Exception ex) {
                    errorLabel.setText("密钥处理错误！");
                    return;
                }

                // 进行解密
                String decryptedBinary = decryptAsciiInput(ciphertext, K1, K2);

                // 输出解密结果为二进制
                String result = decryptedBinary;
                outputField.setText(result);
            }
        });

        // 显示窗口
        frame.pack();
        frame.setLocationRelativeTo(null);  // 居中显示
        frame.setVisible(true);
    }
    // 辅助方法，用于添加组件到 GridBagLayout
    private static void addComponent(Container container, JComponent component, int x, int y, GridBagConstraints gbc) {
        gbc.gridx = x;
        gbc.gridy = y;
        container.add(component, gbc);
    }
    // 对 ASCII 输入按 8bit 一组加密
    public static String encryptAsciiInput(String binaryInput, String K1, String K2) {
        StringBuilder ciphertext = new StringBuilder();
        for (int i = 0; i < binaryInput.length(); i += 8) {
            String block = binaryInput.substring(i, Math.min(i + 8, binaryInput.length()));
            ciphertext.append(encrypt(block, K1, K2));  // 对每个8bit块加密
        }
        return ciphertext.toString();  // 合并加密结果
    }

    // 对二进制输入按 8bit 一组加密（保持原样）
    // 此函数与 encryptAsciiInput 相同，保持逻辑不变
    // 这里可以复用 encryptAsciiInput

    // 对 ASCII 输入按 8bit 一组解密
    public static String decryptAsciiInput(String binaryInput, String K1, String K2) {
        StringBuilder decryptedText = new StringBuilder();
        for (int i = 0; i < binaryInput.length(); i += 8) {
            String block = binaryInput.substring(i, Math.min(i + 8, binaryInput.length()));
            decryptedText.append(decrypt(block, K1, K2));  // 对每个8bit块解密
        }
        return decryptedText.toString();  // 合并解密结果
    }

    // ASCII 转换为二进制
    public static String asciiToBinary(String ascii) {
        StringBuilder binary = new StringBuilder();
        for (char c : ascii.toCharArray()) {
            binary.append(String.format("%8s", Integer.toBinaryString(c)).replace(' ', '0'));
        }
        return binary.toString();
    }

    // 二进制转换为 ASCII
    public static String binaryToAscii(String binary) {
        StringBuilder ascii = new StringBuilder();
        for (int i = 0; i < binary.length(); i += 8) {
            String byteString = binary.substring(i, Math.min(i + 8, binary.length()));
            int charCode = Integer.parseInt(byteString, 2);
            ascii.append((char) charCode);
        }
        return ascii.toString();
    }

    // 置换操作
    public static String replace(String str, int[] arr, int len) {
        char[] item = new char[len];
        for (int i = 0; i < len; i++) {
            int index = arr[i] - 1;
            item[i] = str.charAt(index);
        }
        return new String(item);
    }

    // 循环左移操作
    public static String leftMove(String str, int times) {
        int len = str.length();
        int mid = len / 2;
        String leftPart = str.substring(0, mid);
        String rightPart = str.substring(mid);
        String leftMoved = leftPart.substring(times % mid) + leftPart.substring(0, times % mid);
        String rightMoved = rightPart.substring(times % (len - mid)) + rightPart.substring(0, times % (len - mid));
        return leftMoved + rightMoved;
    }

    // XOR 操作
    public static String xorOperation(String str1, String str2) {
        int len = str1.length();
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < len; i++) {
            result.append(str1.charAt(i) == str2.charAt(i) ? '0' : '1');
        }
        return result.toString();
    }

    // 二进制转换为十进制
    public static int binaryToDecimal(String binary) {
        return Integer.parseInt(binary, 2);
    }

    // F 函数及 S 盒替换
    public static String RandKtoS(String R, String K) {
        R = replace(R, EP, 8);
        String XOR = xorOperation(R, K);
        String S1 = XOR.substring(0, 4);
        String S2 = XOR.substring(4);

        int S1H = binaryToDecimal("" + S1.charAt(0) + S1.charAt(3));  // S1 的第一位和第四位作为行号
        int S1L = binaryToDecimal(S1.substring(1, 3));                // S1 的第二位和第三位作为列号
        int S2H = binaryToDecimal("" + S2.charAt(0) + S2.charAt(3));  // S2 的第一位和第四位作为行号
        int S2L = binaryToDecimal(S2.substring(1, 3));                // S2 的第二位和第三位作为列号

        String leftPart = String.format("%2s", Integer.toBinaryString(SS1[S1H][S1L])).replace(' ', '0');
        String rightPart = String.format("%2s", Integer.toBinaryString(SS2[S2H][S2L])).replace(' ', '0');

        return replace(leftPart + rightPart, P4, 4);
    }

    // 加密函数
    public static String encrypt(String plaintext, String K1, String K2) {
        plaintext = replace(plaintext, IP, 8);
        String L0 = plaintext.substring(0, 4);
        String R0 = plaintext.substring(4);
        String L1 = R0;

        String F1 = RandKtoS(R0, K1);
        String R1 = xorOperation(F1, L0);

        String F2 = RandKtoS(R1, K2);
        String L2 = xorOperation(L1, F2);
        String R2 = R1;

        String ciphertext = L2 + R2;
        return replace(ciphertext, IPIN, 8);
    }

    // 解密函数
    public static String decrypt(String ciphertext, String K1, String K2) {
        ciphertext = replace(ciphertext, IP, 8);
        String L0 = ciphertext.substring(0, 4);
        String R0 = ciphertext.substring(4);
        String L1 = R0;

        String F1 = RandKtoS(R0, K2);
        String R1 = xorOperation(F1, L0);

        String F2 = RandKtoS(R1, K1);
        String L2 = xorOperation(L1, F2);
        String R2 = R1;

        String plaintext = L2 + R2;
        return replace(plaintext, IPIN, 8);
    }
}
