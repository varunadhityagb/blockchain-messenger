package blockchain;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class BlockChainView {
    private BlockChain blockChain;

    public void displayBlockChain() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, IOException {
        for (int i = 0; i < blockChain.size(); i++) {
            Block block = blockChain.getBlock(i);
            //System.out.println("Block " + i + ":");
            // System.out.println("Previous Hash: " + block.getPreviousHash());
//            System.out.println("Hash: " + block.hash);
//            System.out.println("Message: " + block.getMessage());
            // System.out.println("Sender: " + block.getMessage().getSender());
            // System.out.println("Receiver: " + block.getMessage().getReceiver());
            System.out.println(block);
            System.out.println();
        }
    }

    public static void main(String[] args) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, ClassNotFoundException, IOException {
        BlockChainView blockChainView = new BlockChainView();
        blockChainView.blockChain = BlockChain.deserializeBlockChain("blockchain.ser");
        blockChainView.displayBlockChain();
    }
}
