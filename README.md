<!-- GFM-TOC -->
* [1. 整体架构](#1-整体架构)
* [2. 使用方法](#2-使用方法)
* [3. 项目梳理](#3-项目梳理)
    * [1. Huffman压缩](#1-huffman压缩)
        * [压缩](#压缩)
        * [解压缩](#解压缩)
    * [2. 文件IO工具类](#2-文件io工具类)
        * [编码输出](#编码输出)
        * [编码读入](#编码读入)
<!-- GFM-TOC -->




# 1. 整体架构

<div align=center><img src="https://gitee.com/bankarian/picStorage/raw/master/20200823155604.png" height="30%" width="30%" /></div>

# 2. 使用方法

程序入口位于`client/`下的`ZipUI.java`。



# 3. 项目梳理

## 1. Huffman压缩

> `HuffmanZip.java`

### 压缩

一个文本就是256个字符的组合，这之中必然有部分字符出现的频率更高，一部分出现的频率更低。对于不定长的编码，一种压缩的思路就是出现 **频率高的字符编码尽量短，反之编码可以长一些** 。简而言之：**非均匀地更加合理地分配空间**。

对应到树上为频率高的字符更靠近根，频率低的字符更远离根。

#### 哈夫曼树的构建

哈夫曼树采用一种 **贪心** 的思想，**自底向上构建**。树上的每一个结点有一个`频率`属性，用于比较；仅在叶子结点存储字符。假设当前文本的所有字符的集合为S，那么构建的过程如下：

- 每一次从S中选出两个频率最低的字符，分别创建成两个树上结点，接着将这两个字符的`频率`相加衍生出一个父结点，父结点再作为一个字符放回集合S中。
- 重复以上操作，直至S集合中只有一个结点。这个结点就是 **根** ，保存了所有字符出现频率的总和。

可以发现哈夫曼的构建是一个逐步 **合二为一** 的过程，所以每一个结点要么没有子结点、要么有两个子结点，**故哈夫曼树是一棵满二叉树**；同时由于每一次都是取出频率最小的两个结点，**自底向上**，故频率越小的结点会在越下边，这样就能够实现频率小编码长、频率高编码短的目的。

#### 编码

构建完Huffman树之后，编码的获取就容易多了，只需遍历整个二叉树，一路上左0右1，到达叶子结点时构成的01串就是该叶子结点字符的编码。

#### 数据传输

有了哈夫曼编码表之后，原先的文件可以通过编码表一一转换成哈夫曼编码后传输。但是光传编码出去是不够的，别人对着你转换后一大串0101只能一脸蒙圈，所以还需要将 **解码的方法** 传出去（这也是一种难以避免的空间开销），两种方式：

1. 传编码表。

   对于这种方式我所能想到的是（字符ascii，编码）这样一对对地传，这样的话由于编码是不定长的，所以每一对的界限不清晰，对方并不能解析；

   当然你也可以（字符ascii，频率）这样传，但是这样传频率至少需要一个`int`类型的大小来存，空间消耗有些大。并且 **频率的具体值并不是我们所关心的，我们关心的是频率的大小关系** 。所以我更趋向于第二种传输方式。

2. 传哈夫曼树。

   树不是一种线性的结构，我们需要把其转换成线性的结构才能够用01串传输出去。将 **树以线性形式表示** 就是**遍历**了，所以传输的是**哈夫曼树遍历的序列**。

   假设规定好传输前序遍历序列， **以0表示分支结点，以1表示叶子结点** ，一旦遇到叶子结点就补上叶子结点的字符信息（8bit/1byte）。这样之后在解码的时候，只需要按照前序遍历的逻辑来分析01串：一旦遇到1，则后边8bit表示的是该叶子结点处的字符。

<img src="https://gitee.com/bankarian/picStorage/raw/master/20200723205522.png" style="zoom:50%;" />

实现了哈夫曼对文件的重新编码后，再加上对文件的读入输出，就是一个简单的压缩软件了。大体的思路如下：

定义压缩文本的构成：哈夫曼树 	编码后的文本

- 压缩：
  - 读入文件内容
  - 统计每一个字符的出现频率
  - 根据频率构建哈夫曼树，获得哈夫曼编码
  - 将哈夫曼树前序序列写入新的目标文件
  - 根据编码表，将文本逐个字符重新编码，写入目标文件
  - 压缩完成

### 解压缩

解压缩部分就非常的容易了，大体思路：

- 解压缩：
  - 读入哈夫曼树
  - 读入重新编码后的文本
  - 根据哈夫曼树进行解码，解码结果输出
  - 解压缩完成

#### 读入哈夫曼树

由于已经规定好传输的是树的 **前序遍历序列** ，所以利用**根左右**模式的递归可以很方便读入整个哈夫曼树：

```java
private Node readTrie() {
  boolean isLeaf = BinaryInputUtil.readBoolean();
  if (isLeaf)	// 是叶子，则读入叶子存储的字节信息
    return new Node(BinaryInputUtil.readChar(), -1, null, null);
  else	// 不是叶子，则继续前序遍历
    return new Node('\0', -1, readTrie(), readTrie());
}
```

#### 解码

得到了哈夫曼树，解码就非常容易了，只需要不断从文件中读入01串，同时遍历树（左零右一），一旦遇到了叶子则说明解出了一个字节。

```java
Node root = readTrie(), n;
while (!BinaryInputUtil.isEmpty()) {
  n = root;	// 从根开始随着01的读入遍历Huffman树
  while (!n.isLeaf()) {
    boolean bit = BinaryInputUtil.readBoolean();
    if (bit) n = n.right;
    else n = n.left;
  }
  BinaryOutputUtil.write(n.ch, 8);	// 遇到叶子结点，解码出一个字节
}
```



## 2. 文件IO工具类

> `BinaryOutputUtil.java` `BinaryInputUtil.java`
>
> 无论是读/写文件，最小操作单元都是`Byte`，所以我们的数据无论是写入还是传出文件都应该是 **一个字节一个字节** 地操作。

### 编码输出

我们的哈夫曼编码是 **位级的编码** （`bit`），所以不能够一个个编码地直接写入文件。可以通过一个中间层的缓存变量`buffer`，利用位操作来缓存当前获得的`bit`，一旦存储的`bit`总数达到了8，**即恰好组成了一个`Byte`再将其写入输出流文件**。

```java
/**
* 将特定的bit输出到文件（逻辑上）
*/
private static void writeBit(boolean bit) {
  if (!isInitialized) initialize();

  // 将bit先缓存道输出缓存
  buffer <<= 1;
  if (bit) buffer |= 1;	

  // 达到了8bit，才真正将缓存的数据写出
  n++;
  if (n == 8) clearBuffer();
}

/** 
* 将buffer中的所有数据写出，用0补齐低位
*/
private static void clearBuffer() {
  if (!isInitialized) initialize();

  if (n == 0) return;	// 缓存中没有数据，无需写出
  if (n > 0) buffer <<= (8 - n);	// 去掉前导零
  try {
    out.write(buffer);	// out是一个绑定了文件的输出流，这里真正将一个byte输出
  } catch (IOException e) {
    e.printStackTrace();
  }
  n = 0;
  buffer = 0;
}
```

这就是一个 **逻辑上** 输出一个比特到文件的逻辑：**物理上并没有立即写出去，而是先暂时存在了程序的缓存变量中**。由于计算机中最小的单位就是比特，所以有了比特的输出方式，其余的所有数据类型都可以输出了，例如输出一个字节：

```java
/**
* Writes the 8-bit byte to  output.
*/
private static void writeByte(int x) {
  if (!isInitialized) initialize();

  assert x >= 0 && x < 256;

  // 若字节恰好能够补齐，直接写出
  if (n == 0) {
    try {
      out.write(x);
    } catch (IOException e) {
      e.printStackTrace();
    }
    return;
  }

  // 否则，一个个bit地缓存到buffer中再输出
  for (int i = 0; i < 8; i++) {
    boolean bit = ((x >>> (8 - i - 1)) & 1) == 1;
    writeBit(bit);
  }
}
```

我使用的输出流是`BufferedOutputStream`，在API中提到过：每次调用`write`方法实际上是 **将数据写到了这个流的隐藏缓存中** ，并没有真正写出。要将数据真正写出，有两种情况：

1. 在存储的数据长度超过了流缓存长度，自动将数据真正输出到输出流；
2. 或者调用`flush`方法，强制将流缓存中的数据真正放到输出流。

故在这种特性下，我的整个输出编码的逻辑就是：编码一个比特一个比特地存入自定义IO工具的`buffer`中，`buffer`会根据当前的缓存情况将数据存入到`BufferedOutputStream`的流缓存中，最终刷出流缓存的数据完成编码输出到文件。

<img src="https://gitee.com/bankarian/picStorage/raw/master/20200823151919.png" style="zoom:50%;" />

### 编码读入

编码的读入与输出部分非常类似，同样是因为文件的所有操作都是字节为单位的，故只能将数据一个字节一个字节地读入。同样在输入工具类`BinaryInputUtil.java`定义一个缓存`buffer`，缓存每一次读入的 **一个字节** ，接着根据需要的具体数据类型进行比特的转换。

<div align=center><img src="https://gitee.com/bankarian/picStorage/raw/master/20200823152956.png" width="30%" height="30%"/></div>

```java
// 从buffer中读入一个bit（用boolean表示）
public static boolean readBoolean() {	
  if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
  n--;
  boolean bit = ((buffer >> n) & 1) == 1;
  if (n == 0) fillBuffer();
  return bit;
}

// 从输入流读入一个字节到buffer
private static void fillBuffer() {
  try {
    buffer = in.read();
    n = 8;
  } catch (IOException e) {
    System.out.println("EOF");
    buffer = EOF;
    n = -1;
  }
}
```

有了比特的读入，通过一定的组合来表示成其他的具体类型，例如读入一个字节就是连续读入8个比特。

```java
public static char readChar() {
  if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");

  // 若缓存恰好就存着8个bit，直接获取值并更新buffer
  if (n == 8) {
    int x = buffer;
    fillBuffer();
    return (char) (x & 0xff);
  }

  // 否则只能一个个bit地组合成一个字节
  int x = buffer, oldN = n;
  x <<= (8 - n);	// 把剩余的bit先读入
  fillBuffer();		// 重新从输入流中获取一个字节到buffer
  if (isEmpty()) throw new NoSuchElementException("Reading from empty input stream");
  n = oldN;	// 组合完一个字节后，当前buffer剩余的bit个数
  x |= (buffer >>> n);
  return (char) (x & 0xff);
}
```

那么读入一个`int`整数就是连续读入4个字节。其余类型的数据都大同小异，就不一一赘述了。

```java
public static int readInt() {
  int x = 0;
  for (int i = 0; i < 4; i++) {
    char c = readChar();
    x <<= 8;
    x |= c;
  }
  return x;
}
```

