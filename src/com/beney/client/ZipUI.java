package com.beney.client;



import com.beney.huffman.HuffmanZip;
import com.beney.interfaces.MyZip;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

public class ZipUI
        extends JFrame implements ActionListener {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ZipUI().createAndShowGUI());
    }

    private File opened_file, other_file;
    private long past, future;
    private JLabel redLabel, blueLabel, redScore, blueScore;
    private JPanel buttonPanel;
    private JButton compressBtn, expandBtn;
    private MyZip zip = new HuffmanZip();


    public JPanel createContentPane() {
        // We create a bottom JPanel to place everything on.
        JPanel totalGUI = new JPanel();
        totalGUI.setLayout(null);

        JPanel titlePanel = new JPanel();
        titlePanel.setLayout(null);
        titlePanel.setLocation(90, 20);
        titlePanel.setSize(170, 70);
        totalGUI.add(titlePanel);

        redLabel = new JLabel("初始文件大小: ");
        redLabel.setLocation(43, 0);
        redLabel.setSize(150, 30);
        redLabel.setHorizontalAlignment(0);
        titlePanel.add(redLabel);

        blueLabel = new JLabel("压缩/解压后大小: ");
        blueLabel.setLocation(10, 30);
        blueLabel.setSize(170, 30);
        blueLabel.setHorizontalAlignment(0);
        titlePanel.add(blueLabel);

        JPanel scorePanel = new JPanel();
        scorePanel.setLayout(null);
        scorePanel.setLocation(270, 20);
        scorePanel.setSize(320, 60);
        totalGUI.add(scorePanel);

        redScore = new JLabel("");
        redScore.setLocation(0, 0);
        redScore.setSize(300, 30);
        redScore.setHorizontalAlignment(0);
        scorePanel.add(redScore);

        blueScore = new JLabel("");
        blueScore.setLocation(0, 30);
        blueScore.setSize(300, 30);
        blueScore.setHorizontalAlignment(SwingConstants.CENTER);
        scorePanel.add(blueScore);

        buttonPanel = new JPanel();
        buttonPanel.setLayout(null);
        buttonPanel.setLocation(10, 130);
        buttonPanel.setSize(5200, 150);
        totalGUI.add(buttonPanel);

        compressBtn = new JButton("压缩");
        compressBtn.setLocation(100, 0);
        compressBtn.setSize(120, 30);
        compressBtn.addActionListener(this);
        buttonPanel.add(compressBtn);

        expandBtn = new JButton("解压缩");
        expandBtn.setLocation(290, 0);
        expandBtn.setSize(120, 30);
        expandBtn.addActionListener(this);
        buttonPanel.add(expandBtn);

        totalGUI.setOpaque(true);
        return totalGUI;
    }

    public void actionPerformed(ActionEvent e) {
        if (opened_file == null || opened_file.getPath().length() == 0) {
            JOptionPane.showMessageDialog(
                    null,
                    "请从 File 选项中选择一个文件",
                    "Status",
                    JOptionPane.PLAIN_MESSAGE);
            return;
        }
        if (e.getSource() == compressBtn) {

            zip.compress(opened_file.getPath());
            JOptionPane.showMessageDialog(
                    null,
                    "..........................Zipping Finished..........................",
                    "Status",
                    JOptionPane.PLAIN_MESSAGE);
            redScore.setText(opened_file.length() + "Bytes (" + opened_file.getName() + ")");
            other_file = new File(opened_file.getPath() + zip.fileSuffix());
            future = other_file.length();
            blueScore.setText(future
                    + "Bytes ("
                    + other_file.getName()
                    + ")");
        } else if (e.getSource() == expandBtn) {
            String path = opened_file.getPath();
            if (!path.endsWith(zip.fileSuffix())) {
                JOptionPane.showMessageDialog(
                        null,
                        "要解压的文件后缀名不正确哟~\n请重新在 File 处选择文件",
                        "Status",
                        JOptionPane.PLAIN_MESSAGE);
                return;
            }
            zip.expand(opened_file.getPath());
            JOptionPane.showMessageDialog(
                    null,
                    "..........................UnZipping Finished..........................",
                    "Status",
                    JOptionPane.PLAIN_MESSAGE);
            redScore.setText(opened_file.length()
                    + "Bytes ("
                    + opened_file.getName()
                    + ")");
            String s = opened_file.getPath();
            s = s.substring(0, s.length() - zip.fileSuffix().length());
            other_file = new File(s);
            future = other_file.length();
            blueScore.setText(future
                    + "Bytes ("
                    + other_file.getName()
                    + ")");
        }
    }

    private void createAndShowGUI() {
        JFrame frame = new JFrame("Huffman Zip by Bankarian");
        frame.setContentPane(this.createContentPane());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBounds(350, 170, 550, 300);

        JMenu fileMenu = new JMenu("File");

        JMenuBar bar = new JMenuBar();
        frame.setJMenuBar(bar);
        bar.add(fileMenu);

        JMenuItem openItem = new JMenuItem("Open");
        fileMenu.add(openItem);
        openItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.showDialog(new JLabel(), "选择文件");
            opened_file = fileChooser.getSelectedFile();
            past = opened_file.length();
            redScore.setText(past
                    + "Bytes ("
                    + opened_file.getName()
                    + ")");
            blueScore.setText("NotYetCalculated");
        });

        JMenu helpMenu = new JMenu("Help");
        frame.setJMenuBar(bar);
        bar.add(helpMenu);

        JMenuItem helpItem = new JMenuItem("How To");
        helpMenu.add(helpItem);
        helpItem.addActionListener(event -> JOptionPane.showMessageDialog(
                null,
                "基于哈夫曼编码的算法实现" + "\n" + "在 File 选项中找到要压缩或者要解压的文件" + "\n" + "压缩完成的文件会多出 .huf 后缀名" + "\n"
                        + "只支持解压 .huf 后缀的压缩文件，解压后的文件 .huf 后缀消失" + "\n",
                "How To...",
                JOptionPane.PLAIN_MESSAGE));
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(aboutItem);

        aboutItem.addActionListener(event -> JOptionPane.showMessageDialog(
                null,
                "此软件利用Java开发，作者Beney" + "\n",
                "About",
                JOptionPane.PLAIN_MESSAGE));
        frame.setVisible(true);
    }

}
