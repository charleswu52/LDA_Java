package la;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author WuChao
 * @since 2021/5/15 10:41
 */


public class MDAG {
    private MDAGNode sourceNode = new MDAGNode(false);
    private SimpleMDAGNode simplifiedSourceNode;
    private HashMap<MDAGNode, MDAGNode> equivalenceClassMDAGNodeHashMap = new HashMap();
    private SimpleMDAGNode[] mdagDataArray;
    private TreeSet<Character> charTreeSet = new TreeSet();
    private int transitionCount;

    public MDAG(File dataFile) throws IOException {
        BufferedReader dataFileBufferedReader = new BufferedReader(new FileReader(dataFile));
        String currentString = "";

        String previousString;
        for (previousString = ""; (currentString = dataFileBufferedReader.readLine()) != null; previousString = currentString) {
            int mpsIndex = this.calculateMinimizationProcessingStartIndex(previousString, currentString);
            if (mpsIndex != -1) {
                String transitionSubstring = previousString.substring(0, mpsIndex);
                String minimizationProcessingSubstring = previousString.substring(mpsIndex);
                this.replaceOrRegister(this.sourceNode.transition(transitionSubstring), minimizationProcessingSubstring);
            }

            this.addStringInternal(currentString);
        }

        this.replaceOrRegister(this.sourceNode, previousString);
    }

    public MDAG(Collection<String> strCollection) {
        this.addStrings(strCollection);
    }

    public final void addStrings(Collection<String> strCollection) {
        if (this.sourceNode != null) {
            String previousString = "";

            String currentString;
            for (Iterator i$ = strCollection.iterator(); i$.hasNext(); previousString = currentString) {
                currentString = (String) i$.next();
                int mpsIndex = this.calculateMinimizationProcessingStartIndex(previousString, currentString);
                if (mpsIndex != -1) {
                    String transitionSubstring = previousString.substring(0, mpsIndex);
                    String minimizationProcessingSubString = previousString.substring(mpsIndex);
                    this.replaceOrRegister(this.sourceNode.transition(transitionSubstring), minimizationProcessingSubString);
                }

                this.addStringInternal(currentString);
            }

            this.replaceOrRegister(this.sourceNode, previousString);
        } else {
            throw new UnsupportedOperationException("la.MDAG is simplified. Unable to add additional Strings.");
        }
    }

    public void addString(String str) {
        if (this.sourceNode != null) {
            this.addStringInternal(str);
            this.replaceOrRegister(this.sourceNode, str);
        } else {
            throw new UnsupportedOperationException("la.MDAG is simplified. Unable to add additional Strings.");
        }
    }

    private void splitTransitionPath(MDAGNode originNode, String storedStringSubstr) {
        HashMap<String, Object> firstConfluenceNodeDataHashMap = this.getTransitionPathFirstConfluenceNodeData(originNode, storedStringSubstr);
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer) firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        MDAGNode firstConfluenceNode = (MDAGNode) firstConfluenceNodeDataHashMap.get("confluenceNode");
        if (firstConfluenceNode != null) {
            MDAGNode firstConfluenceNodeParent = originNode.transition(storedStringSubstr.substring(0, toFirstConfluenceNodeTransitionCharIndex));
            MDAGNode firstConfluenceNodeClone = firstConfluenceNode.clone(firstConfluenceNodeParent, storedStringSubstr.charAt(toFirstConfluenceNodeTransitionCharIndex));
            this.transitionCount += firstConfluenceNodeClone.getOutgoingTransitionCount();
            String unprocessedSubString = storedStringSubstr.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            this.splitTransitionPath(firstConfluenceNodeClone, unprocessedSubString);
        }

    }

    private int calculateSoleTransitionPathLength(String str) {
        Stack<MDAGNode> transitionPathNodeStack = this.sourceNode.getTransitionPathNodes(str);
        transitionPathNodeStack.pop();
        transitionPathNodeStack.trimToSize();

        while (!transitionPathNodeStack.isEmpty()) {
            MDAGNode currentNode = (MDAGNode) transitionPathNodeStack.peek();
            if (currentNode.getOutgoingTransitions().size() > 1 || currentNode.isAcceptNode()) {
                break;
            }

            transitionPathNodeStack.pop();
        }

        return transitionPathNodeStack.capacity() - transitionPathNodeStack.size();
    }

    public void removeString(String str) {
        if (this.sourceNode != null) {
            this.splitTransitionPath(this.sourceNode, str);
            this.removeTransitionPathRegisterEntries(str);
            MDAGNode strEndNode = this.sourceNode.transition(str);
            if (!strEndNode.hasTransitions()) {
                int soleInternalTransitionPathLength = this.calculateSoleTransitionPathLength(str);
                int internalTransitionPathLength = str.length() - 1;
                if (soleInternalTransitionPathLength == internalTransitionPathLength) {
                    this.sourceNode.removeOutgoingTransition(str.charAt(0));
                    this.transitionCount -= str.length();
                } else {
                    int toBeRemovedTransitionLabelCharIndex = internalTransitionPathLength - soleInternalTransitionPathLength;
                    MDAGNode latestNonSoloTransitionPathNode = this.sourceNode.transition(str.substring(0, toBeRemovedTransitionLabelCharIndex));
                    latestNonSoloTransitionPathNode.removeOutgoingTransition(str.charAt(toBeRemovedTransitionLabelCharIndex));
                    this.transitionCount -= str.substring(toBeRemovedTransitionLabelCharIndex).length();
                    this.replaceOrRegister(this.sourceNode, str.substring(0, toBeRemovedTransitionLabelCharIndex));
                }
            } else {
                strEndNode.setAcceptStateStatus(false);
                this.replaceOrRegister(this.sourceNode, str);
            }

        } else {
            throw new UnsupportedOperationException("la.MDAG is simplified. Unable to remove any Strings.");
        }
    }

    private int calculateMinimizationProcessingStartIndex(String prevStr, String currStr) {
        int mpsIndex;
        if (!currStr.startsWith(prevStr)) {
            int shortestStringLength = Math.min(prevStr.length(), currStr.length());

            for (mpsIndex = 0; mpsIndex < shortestStringLength && prevStr.charAt(mpsIndex) == currStr.charAt(mpsIndex); ++mpsIndex) {
            }
        } else {
            mpsIndex = -1;
        }

        return mpsIndex;
    }

    private String determineLongestPrefixInMDAG(String str) {
        MDAGNode currentNode = this.sourceNode;
        int numberOfChars = str.length();
        int onePastPrefixEndIndex = 0;

        for (int i = 0; i < numberOfChars; ++onePastPrefixEndIndex) {
            char currentChar = str.charAt(i);
            if (!currentNode.hasOutgoingTransition(currentChar)) {
                break;
            }

            currentNode = currentNode.transition(currentChar);
            ++i;
        }

        return str.substring(0, onePastPrefixEndIndex);
    }

    private HashMap<String, Object> getTransitionPathFirstConfluenceNodeData(MDAGNode originNode, String str) {
        int currentIndex = 0;
        int charCount = str.length();

        MDAGNode currentNode;
        for (currentNode = originNode; currentIndex < charCount; ++currentIndex) {
            char currentChar = str.charAt(currentIndex);
            currentNode = currentNode.hasOutgoingTransition(currentChar) ? currentNode.transition(currentChar) : null;
            if (currentNode == null || currentNode.isConfluenceNode()) {
                break;
            }
        }

        boolean noConfluenceNode = currentNode == originNode || currentIndex == charCount;
        HashMap<String, Object> confluenceNodeDataHashMap = new HashMap(2);
        confluenceNodeDataHashMap.put("toConfluenceNodeTransitionCharIndex", noConfluenceNode ? null : currentIndex);
        confluenceNodeDataHashMap.put("confluenceNode", noConfluenceNode ? null : currentNode);
        return confluenceNodeDataHashMap;
    }

    private void replaceOrRegister(MDAGNode originNode, String str) {
        char transitionLabelChar = str.charAt(0);
        MDAGNode relevantTargetNode = originNode.transition(transitionLabelChar);
        if (relevantTargetNode.hasTransitions() && !str.substring(1).isEmpty()) {
            this.replaceOrRegister(relevantTargetNode, str.substring(1));
        }

        MDAGNode equivalentNode = (MDAGNode) this.equivalenceClassMDAGNodeHashMap.get(relevantTargetNode);
        if (equivalentNode == null) {
            this.equivalenceClassMDAGNodeHashMap.put(relevantTargetNode, relevantTargetNode);
        } else if (equivalentNode != relevantTargetNode) {
            relevantTargetNode.decrementTargetIncomingTransitionCounts();
            this.transitionCount -= relevantTargetNode.getOutgoingTransitionCount();
            originNode.reassignOutgoingTransition(transitionLabelChar, relevantTargetNode, equivalentNode);
        }

    }

    private void addTransitionPath(MDAGNode originNode, String str) {
        if (!str.isEmpty()) {
            MDAGNode currentNode = originNode;
            int charCount = str.length();

            for (int i = 0; i < charCount; ++this.transitionCount) {
                char currentChar = str.charAt(i);
                boolean isLastChar = i == charCount - 1;
                currentNode = currentNode.addOutgoingTransition(currentChar, isLastChar);
                this.charTreeSet.add(currentChar);
                ++i;
            }
        } else {
            originNode.setAcceptStateStatus(true);
        }

    }

    private void removeTransitionPathRegisterEntries(String str) {
        MDAGNode currentNode = this.sourceNode;
        int charCount = str.length();

        for (int i = 0; i < charCount; ++i) {
            currentNode = currentNode.transition(str.charAt(i));
            if (this.equivalenceClassMDAGNodeHashMap.get(currentNode) == currentNode) {
                this.equivalenceClassMDAGNodeHashMap.remove(currentNode);
            }

            currentNode.clearStoredHashCode();
        }

    }

    private void cloneTransitionPath(MDAGNode pivotConfluenceNode, String transitionStringToPivotNode, String str) {
        MDAGNode lastTargetNode = pivotConfluenceNode.transition(str);
        MDAGNode lastClonedNode = null;
        char lastTransitionLabelChar = 0;

        for (int i = str.length(); i >= 0; --i) {
            String currentTransitionString = i > 0 ? str.substring(0, i) : null;
            MDAGNode currentTargetNode = i > 0 ? pivotConfluenceNode.transition(currentTransitionString) : pivotConfluenceNode;
            MDAGNode clonedNode;
            if (i == 0) {
                String transitionStringToPivotNodeParent = transitionStringToPivotNode.substring(0, transitionStringToPivotNode.length() - 1);
                char parentTransitionLabelChar = transitionStringToPivotNode.charAt(transitionStringToPivotNode.length() - 1);
                clonedNode = pivotConfluenceNode.clone(this.sourceNode.transition(transitionStringToPivotNodeParent), parentTransitionLabelChar);
            } else {
                clonedNode = currentTargetNode.clone();
            }

            this.transitionCount += clonedNode.getOutgoingTransitionCount();
            if (lastClonedNode != null) {
                clonedNode.reassignOutgoingTransition(lastTransitionLabelChar, lastTargetNode, lastClonedNode);
                lastTargetNode = currentTargetNode;
            }

            lastClonedNode = clonedNode;
            lastTransitionLabelChar = i > 0 ? str.charAt(i - 1) : 0;
        }

    }

    private void addStringInternal(String str) {
        String prefixString = this.determineLongestPrefixInMDAG(str);
        String suffixString = str.substring(prefixString.length());
        HashMap<String, Object> firstConfluenceNodeDataHashMap = this.getTransitionPathFirstConfluenceNodeData(this.sourceNode, prefixString);
        MDAGNode firstConfluenceNodeInPrefix = (MDAGNode) firstConfluenceNodeDataHashMap.get("confluenceNode");
        Integer toFirstConfluenceNodeTransitionCharIndex = (Integer) firstConfluenceNodeDataHashMap.get("toConfluenceNodeTransitionCharIndex");
        this.removeTransitionPathRegisterEntries(toFirstConfluenceNodeTransitionCharIndex == null ? prefixString : prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex));
        if (firstConfluenceNodeInPrefix != null) {
            String transitionStringOfPathToFirstConfluenceNode = prefixString.substring(0, toFirstConfluenceNodeTransitionCharIndex + 1);
            String transitionStringOfToBeDuplicatedPath = prefixString.substring(toFirstConfluenceNodeTransitionCharIndex + 1);
            this.cloneTransitionPath(firstConfluenceNodeInPrefix, transitionStringOfPathToFirstConfluenceNode, transitionStringOfToBeDuplicatedPath);
        }

        this.addTransitionPath(this.sourceNode.transition(prefixString), suffixString);
    }

    private int createSimpleMDAGTransitionSet(MDAGNode node, SimpleMDAGNode[] mdagDataArray, int onePastLastCreatedTransitionSetIndex) {
        int pivotIndex = onePastLastCreatedTransitionSetIndex;
        node.setTransitionSetBeginIndex(onePastLastCreatedTransitionSetIndex);
        onePastLastCreatedTransitionSetIndex += node.getOutgoingTransitionCount();
        TreeMap<Character, MDAGNode> transitionTreeMap = node.getOutgoingTransitions();

        MDAGNode transitionTargetNode;
        for (Iterator i$ = transitionTreeMap.entrySet().iterator(); i$.hasNext(); mdagDataArray[pivotIndex++].setTransitionSetBeginIndex(transitionTargetNode.getTransitionSetBeginIndex())) {
            Map.Entry<Character, MDAGNode> transitionKeyValuePair = (Map.Entry) i$.next();
            char transitionLabelChar = (Character) transitionKeyValuePair.getKey();
            transitionTargetNode = (MDAGNode) transitionKeyValuePair.getValue();
            mdagDataArray[pivotIndex] = new SimpleMDAGNode(transitionLabelChar, transitionTargetNode.isAcceptNode(), transitionTargetNode.getOutgoingTransitionCount());
            if (transitionTargetNode.getTransitionSetBeginIndex() == -1) {
                onePastLastCreatedTransitionSetIndex = this.createSimpleMDAGTransitionSet(transitionTargetNode, mdagDataArray, onePastLastCreatedTransitionSetIndex);
            }
        }

        return onePastLastCreatedTransitionSetIndex;
    }

    public void simplify() {
        if (this.sourceNode != null) {
            this.mdagDataArray = new SimpleMDAGNode[this.transitionCount];
            this.createSimpleMDAGTransitionSet(this.sourceNode, this.mdagDataArray, 0);
            this.simplifiedSourceNode = new SimpleMDAGNode('\u0000', false, this.sourceNode.getOutgoingTransitionCount());
            this.sourceNode = null;
            this.equivalenceClassMDAGNodeHashMap = null;
        }

    }

    public boolean contains(String str) {
        if (this.sourceNode != null) {
            MDAGNode targetNode = this.sourceNode.transition(str);
            return targetNode != null && targetNode.isAcceptNode();
        } else {
            SimpleMDAGNode targetNode = SimpleMDAGNode.traverseMDAG(this.mdagDataArray, this.simplifiedSourceNode, str);
            return targetNode != null && targetNode.isAcceptNode();
        }
    }

    private void getStrings(HashSet<String> strHashSet, MDAG.SearchCondition searchCondition, String searchConditionString, String prefixString, TreeMap<Character, MDAGNode> transitionTreeMap) {
        String newPrefixString;
        MDAGNode currentNode;
        for (Iterator i$ = transitionTreeMap.entrySet().iterator(); i$.hasNext(); this.getStrings(strHashSet, searchCondition, searchConditionString, newPrefixString, currentNode.getOutgoingTransitions())) {
            Map.Entry<Character, MDAGNode> transitionKeyValuePair = (Map.Entry) i$.next();
            newPrefixString = prefixString + transitionKeyValuePair.getKey();
            currentNode = (MDAGNode) transitionKeyValuePair.getValue();
            if (currentNode.isAcceptNode() && searchCondition.satisfiesCondition(newPrefixString, searchConditionString)) {
                strHashSet.add(newPrefixString);
            }
        }

    }

    private void getStrings(HashSet<String> strHashSet, MDAG.SearchCondition searchCondition, String searchConditionString, String prefixString, SimpleMDAGNode node) {
        int transitionSetBegin = node.getTransitionSetBeginIndex();
        int onePastTransitionSetEnd = transitionSetBegin + node.getOutgoingTransitionSetSize();

        for (int i = transitionSetBegin; i < onePastTransitionSetEnd; ++i) {
            SimpleMDAGNode currentNode = this.mdagDataArray[i];
            String newPrefixString = prefixString + currentNode.getLetter();
            if (currentNode.isAcceptNode() && searchCondition.satisfiesCondition(newPrefixString, searchConditionString)) {
                strHashSet.add(newPrefixString);
            }

            this.getStrings(strHashSet, searchCondition, searchConditionString, newPrefixString, currentNode);
        }

    }

    public HashSet<String> getAllStrings() {
        HashSet<String> strHashSet = new HashSet();
        if (this.sourceNode != null) {
            this.getStrings(strHashSet, MDAG.SearchCondition.NO_SEARCH_CONDITION, (String) null, "", (TreeMap) this.sourceNode.getOutgoingTransitions());
        } else {
            this.getStrings(strHashSet, MDAG.SearchCondition.NO_SEARCH_CONDITION, (String) null, "", (SimpleMDAGNode) this.simplifiedSourceNode);
        }

        return strHashSet;
    }

    public HashSet<String> getStringsStartingWith(String prefixStr) {
        HashSet<String> strHashSet = new HashSet();
        if (this.sourceNode != null) {
            MDAGNode originNode = this.sourceNode.transition(prefixStr);
            if (originNode != null) {
                if (originNode.isAcceptNode()) {
                    strHashSet.add(prefixStr);
                }

                this.getStrings(strHashSet, MDAG.SearchCondition.PREFIX_SEARCH_CONDITION, prefixStr, prefixStr, originNode.getOutgoingTransitions());
            }
        } else {
            SimpleMDAGNode originNode = SimpleMDAGNode.traverseMDAG(this.mdagDataArray, this.simplifiedSourceNode, prefixStr);
            if (originNode != null) {
                if (originNode.isAcceptNode()) {
                    strHashSet.add(prefixStr);
                }

                this.getStrings(strHashSet, MDAG.SearchCondition.PREFIX_SEARCH_CONDITION, prefixStr, prefixStr, originNode);
            }
        }

        return strHashSet;
    }

    public HashSet<String> getStringsWithSubstring(String str) {
        HashSet<String> strHashSet = new HashSet();
        if (this.sourceNode != null) {
            this.getStrings(strHashSet, MDAG.SearchCondition.SUBSTRING_SEARCH_CONDITION, str, "", this.sourceNode.getOutgoingTransitions());
        } else {
            this.getStrings(strHashSet, MDAG.SearchCondition.SUBSTRING_SEARCH_CONDITION, str, "", this.simplifiedSourceNode);
        }

        return strHashSet;
    }

    public HashSet<String> getStringsEndingWith(String suffixStr) {
        HashSet<String> strHashSet = new HashSet();
        if (this.sourceNode != null) {
            this.getStrings(strHashSet, MDAG.SearchCondition.SUFFIX_SEARCH_CONDITION, suffixStr, "", this.sourceNode.getOutgoingTransitions());
        } else {
            this.getStrings(strHashSet, MDAG.SearchCondition.SUFFIX_SEARCH_CONDITION, suffixStr, "", this.simplifiedSourceNode);
        }

        return strHashSet;
    }

    public Object getSourceNode() {
        return this.sourceNode != null ? this.sourceNode : this.simplifiedSourceNode;
    }

    public SimpleMDAGNode[] getSimpleMDAGArray() {
        return this.mdagDataArray;
    }

    public TreeSet<Character> getTransitionLabelSet() {
        return this.charTreeSet;
    }

    public static boolean isAcceptNode(Object nodeObj) {
        if (nodeObj != null) {
            Class nodeObjClass = nodeObj.getClass();
            if (nodeObjClass.equals(MDAGNode.class)) {
                return ((MDAGNode) nodeObj).isAcceptNode();
            }

            if (nodeObjClass.equals(SimpleMDAGNode.class)) {
                return ((SimpleMDAGNode) nodeObj).isAcceptNode();
            }
        }

        throw new IllegalArgumentException("Argument is not an la.MDAGNode or SimpleMDAGNode");
    }

    private static enum SearchCondition {
        NO_SEARCH_CONDITION,
        PREFIX_SEARCH_CONDITION,
        SUBSTRING_SEARCH_CONDITION,
        SUFFIX_SEARCH_CONDITION;

        private SearchCondition() {
        }

        public boolean satisfiesCondition(String str1, String str2) {
            boolean satisfiesSearchCondition;
            switch (this) {
                case PREFIX_SEARCH_CONDITION:
                    satisfiesSearchCondition = str1.startsWith(str2);
                    break;
                case SUBSTRING_SEARCH_CONDITION:
                    satisfiesSearchCondition = str1.contains(str2);
                    break;
                case SUFFIX_SEARCH_CONDITION:
                    satisfiesSearchCondition = str1.endsWith(str2);
                    break;
                default:
                    satisfiesSearchCondition = true;
            }

            return satisfiesSearchCondition;
        }
    }
}
