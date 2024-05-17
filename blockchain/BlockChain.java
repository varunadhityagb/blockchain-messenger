package blockchain;

import java.io.*;
import java.util.LinkedList;

public class BlockChain implements Serializable {
    private LinkedList<Block> blockChain = new LinkedList<>();

    public void addBlock(Block block) {
        blockChain.add(block);
    }

    public Block getLastBlock() {
        return blockChain.getLast();
    }

    public int size() {
        return blockChain.size();
    }

    public void serializeBlockChain(String fileName) {
        try (ObjectOutputStream oos = new ObjectOutputStream((new FileOutputStream((fileName))))) {
            oos.writeObject(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static BlockChain deserializeBlockChain(String fileName) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(fileName))) {
            return (BlockChain) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}