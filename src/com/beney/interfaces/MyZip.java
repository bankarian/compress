package com.beney.interfaces;

/**
 * 文本压缩工具行为规范
 *
 * @author Beney
 */
public interface MyZip {
    /**
     * 压缩文件
     *
     * @param filePath 需要压缩的文件的路径
     */
    void compress(String filePath);

    /**
     * 解压文件
     *
     * @param filePath 需要解压的文件的路径
     */
    void expand(String filePath);

    /**
     *
     * @return 压缩文件的后缀
     */
    String fileSuffix();
}
