package la;

import java.util.Iterator;
import java.util.Map;
import java.util.Stack;
import java.util.TreeMap;

/**
 * @author WuChao
 * @since 2021/5/15 10:42
 */


public class MDAGNode {
    private boolean isAcceptNode;
    private final TreeMap<Character, MDAGNode> outgoingTransitionTreeMap;
    private int incomingTransitionCount = 0;
    private int transitionSetBeginIndex = -1;
    private Integer storedHashCode = null;

    public MDAGNode(boolean isAcceptNode) {
        this.isAcceptNode = isAcceptNode;
        this.outgoingTransitionTreeMap = new TreeMap();
    }

    private MDAGNode(MDAGNode node) {
        this.isAcceptNode = node.isAcceptNode;
        this.outgoingTransitionTreeMap = new TreeMap(node.outgoingTransitionTreeMap);

        Map.Entry transitionKeyValuePair;
        for (Iterator i$ = this.outgoingTransitionTreeMap.entrySet().iterator(); i$.hasNext(); ++((MDAGNode) transitionKeyValuePair.getValue()).incomingTransitionCount) {
            transitionKeyValuePair = (Map.Entry) i$.next();
        }

    }

    public MDAGNode clone() {
        return new MDAGNode(this);
    }

    public MDAGNode clone(MDAGNode soleParentNode, char parentToCloneTransitionLabelChar) {
        MDAGNode cloneNode = new MDAGNode(this);
        soleParentNode.reassignOutgoingTransition(parentToCloneTransitionLabelChar, this, cloneNode);
        return cloneNode;
    }

    public int getTransitionSetBeginIndex() {
        return this.transitionSetBeginIndex;
    }

    public int getOutgoingTransitionCount() {
        return this.outgoingTransitionTreeMap.size();
    }

    public int getIncomingTransitionCount() {
        return this.incomingTransitionCount;
    }

    public boolean isConfluenceNode() {
        return this.incomingTransitionCount > 1;
    }

    public boolean isAcceptNode() {
        return this.isAcceptNode;
    }

    public void setAcceptStateStatus(boolean isAcceptNode) {
        this.isAcceptNode = isAcceptNode;
    }

    public void setTransitionSetBeginIndex(int transitionSetBeginIndex) {
        this.transitionSetBeginIndex = transitionSetBeginIndex;
    }

    public boolean hasOutgoingTransition(char letter) {
        return this.outgoingTransitionTreeMap.containsKey(letter);
    }

    public boolean hasTransitions() {
        return !this.outgoingTransitionTreeMap.isEmpty();
    }

    public MDAGNode transition(char letter) {
        return (MDAGNode) this.outgoingTransitionTreeMap.get(letter);
    }

    public MDAGNode transition(String str) {
        int charCount = str.length();
        MDAGNode currentNode = this;

        for (int i = 0; i < charCount; ++i) {
            currentNode = currentNode.transition(str.charAt(i));
            if (currentNode == null) {
                break;
            }
        }

        return currentNode;
    }

    public Stack<MDAGNode> getTransitionPathNodes(String str) {
        Stack<MDAGNode> nodeStack = new Stack();
        MDAGNode currentNode = this;
        int numberOfChars = str.length();

        for (int i = 0; i < numberOfChars && currentNode != null; ++i) {
            currentNode = currentNode.transition(str.charAt(i));
            nodeStack.add(currentNode);
        }

        return nodeStack;
    }

    public TreeMap<Character, MDAGNode> getOutgoingTransitions() {
        return this.outgoingTransitionTreeMap;
    }

    public void decrementTargetIncomingTransitionCounts() {
        Map.Entry transitionKeyValuePair;
        for (Iterator i$ = this.outgoingTransitionTreeMap.entrySet().iterator(); i$.hasNext(); --((MDAGNode) transitionKeyValuePair.getValue()).incomingTransitionCount) {
            transitionKeyValuePair = (Map.Entry) i$.next();
        }

    }

    public void reassignOutgoingTransition(char letter, MDAGNode oldTargetNode, MDAGNode newTargetNode) {
        --oldTargetNode.incomingTransitionCount;
        ++newTargetNode.incomingTransitionCount;
        this.outgoingTransitionTreeMap.put(letter, newTargetNode);
    }

    public MDAGNode addOutgoingTransition(char letter, boolean targetAcceptStateStatus) {
        MDAGNode newTargetNode = new MDAGNode(targetAcceptStateStatus);
        ++newTargetNode.incomingTransitionCount;
        this.outgoingTransitionTreeMap.put(letter, newTargetNode);
        return newTargetNode;
    }

    public void removeOutgoingTransition(char letter) {
        this.outgoingTransitionTreeMap.remove(letter);
    }

    public static boolean haveSameTransitions(MDAGNode node1, MDAGNode node2) {
        TreeMap<Character, MDAGNode> outgoingTransitionTreeMap1 = node1.outgoingTransitionTreeMap;
        TreeMap<Character, MDAGNode> outgoingTransitionTreeMap2 = node2.outgoingTransitionTreeMap;
        if (outgoingTransitionTreeMap1.size() != outgoingTransitionTreeMap2.size()) {
            return false;
        } else {
            Iterator i$ = outgoingTransitionTreeMap1.entrySet().iterator();

            Character currentCharKey;
            MDAGNode currentTargetNode;
            do {
                if (!i$.hasNext()) {
                    return true;
                }

                Map.Entry<Character, MDAGNode> transitionKeyValuePair = (Map.Entry) i$.next();
                currentCharKey = (Character) transitionKeyValuePair.getKey();
                currentTargetNode = (MDAGNode) transitionKeyValuePair.getValue();
            } while (outgoingTransitionTreeMap2.containsKey(currentCharKey) && ((MDAGNode) outgoingTransitionTreeMap2.get(currentCharKey)).equals(currentTargetNode));

            return false;
        }
    }

    public void clearStoredHashCode() {
        this.storedHashCode = null;
    }

    public boolean equals(Object obj) {
        boolean areEqual = this == obj;
        if (!areEqual && obj != null && obj.getClass().equals(MDAGNode.class)) {
            MDAGNode node = (MDAGNode) obj;
            areEqual = this.isAcceptNode == node.isAcceptNode && haveSameTransitions(this, node);
        }

        return areEqual;
    }

    public int hashCode() {
        if (this.storedHashCode == null) {
            int hash = 7;
            hash = 53 * hash + (this.isAcceptNode ? 1 : 0);
            hash = 53 * hash + (this.outgoingTransitionTreeMap != null ? this.outgoingTransitionTreeMap.hashCode() : 0);
            this.storedHashCode = hash;
            return hash;
        } else {
            return this.storedHashCode;
        }
    }
}
