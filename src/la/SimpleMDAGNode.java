package la;

/**
 * @author WuChao
 * @since 2021/5/15 10:43
 */


public class SimpleMDAGNode {
    private final char letter;
    private final boolean isAcceptNode;
    private final int transitionSetSize;
    private int transitionSetBeginIndex;

    public SimpleMDAGNode(char letter, boolean isAcceptNode, int transitionSetSize) {
        this.letter = letter;
        this.isAcceptNode = isAcceptNode;
        this.transitionSetSize = transitionSetSize;
        this.transitionSetBeginIndex = 0;
    }

    public char getLetter() {
        return this.letter;
    }

    public boolean isAcceptNode() {
        return this.isAcceptNode;
    }

    public int getTransitionSetBeginIndex() {
        return this.transitionSetBeginIndex;
    }

    public int getOutgoingTransitionSetSize() {
        return this.transitionSetSize;
    }

    public void setTransitionSetBeginIndex(int transitionSetBeginIndex) {
        this.transitionSetBeginIndex = transitionSetBeginIndex;
    }

    public SimpleMDAGNode transition(SimpleMDAGNode[] mdagDataArray, char letter) {
        int onePastTransitionSetEndIndex = this.transitionSetBeginIndex + this.transitionSetSize;
        SimpleMDAGNode targetNode = null;

        for (int i = this.transitionSetBeginIndex; i < onePastTransitionSetEndIndex; ++i) {
            if (mdagDataArray[i].getLetter() == letter) {
                targetNode = mdagDataArray[i];
                break;
            }
        }

        return targetNode;
    }

    public SimpleMDAGNode transition(SimpleMDAGNode[] mdagDataArray, String str) {
        SimpleMDAGNode currentNode = this;
        int numberOfChars = str.length();

        for (int i = 0; i < numberOfChars; ++i) {
            currentNode = currentNode.transition(mdagDataArray, str.charAt(i));
            if (currentNode == null) {
                break;
            }
        }

        return currentNode;
    }

    public static SimpleMDAGNode traverseMDAG(SimpleMDAGNode[] mdagDataArray, SimpleMDAGNode sourceNode, String str) {
        char firstLetter = str.charAt(0);

        for (int i = 0; i < sourceNode.transitionSetSize; ++i) {
            if (mdagDataArray[i].getLetter() == firstLetter) {
                return mdagDataArray[i].transition(mdagDataArray, str.substring(1));
            }
        }

        return null;
    }
}
