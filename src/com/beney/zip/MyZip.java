package com.beney.zip;

/**
 *
 *
 * @author Beney
 */
public interface MyZip {
    /**
     * compress file
     *
     * @param filePath file path
     */
    void compress(String filePath);

    /**
     * uncompress file
     *
     * @param filePath file path
     */
    void expand(String filePath);

    /**
     *
     * @return suffix of compressed file
     */
    String fileSuffix();
}
