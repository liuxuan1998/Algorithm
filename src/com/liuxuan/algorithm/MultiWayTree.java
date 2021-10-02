package com.liuxuan.algorithm;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *
 * 1.将一个Collections集合自动构建成树  并且检查是否可以构建成树---》构建不成功时报错还是将没有挂在根节点之下的节点过滤  提供选项
 * 2.增删改查节点还是可以自动维护树的状态
 * 3.可以将一颗新的树合并到老树之中，提供如何合并的方法--》注意要求合并时比较的节点key是同一种类型
 *
 */
public class MultiWayTree<T,V> {


    static class TreeNode<T,V>{
        private V key;  //当前节点key
        private V pKey; //父节点Key
        private TreeNode<T,V> parentNode;    //父节点
        private List<TreeNode<T,V>> subNodes;// 子树
        private T data;  //节点的数据



        private boolean add(TreeNode<T,V> node){
            if (subNodes == null){
                subNodes = new LinkedList<>();
            }
            if (node.pKey == this.key){
                return subNodes.add(node);
            }
            return false;
        }

        TreeNode(V key, V pKey, T data){
            this.key = key;
            this.pKey = pKey;
            this.data = data;
        }
        public V getpKey() {
            return pKey;
        }

        public void setpKey(V pKey) {
            this.pKey = pKey;
        }

        public TreeNode<T, V> getParentNode() {
            return parentNode;
        }

        public void setParentNode(TreeNode<T, V> parentNode) {
            this.parentNode = parentNode;
        }


        private List<TreeNode<T,V>> getSubNodes(){
            return subNodes;
        }

        private void setData(T data){
            this.data = data;
        }

        public T getData() {
            return data;
        }
    }

    private Function<T, V> pKey;
    private Function<T, V> key;
    private BiFunction<T, T, T> merge;

    private V rootFlag; //根节点标识

    private TreeNode<T,V> root; //根节点  可以是多个

    private final Map<V, TreeNode<T,V>> nodes = new HashMap<>(); //所有节点  包含根节点

    private List<TreeNode<T,V>> notInTreeNodes = new LinkedList<>(); // 无法挂靠在树上的节点

    public MultiWayTree(Collection<T> datas, Function<T,V> pKey, Function<T,V> key, V rootFlag, BiFunction<T, T, T> merge){
        this.merge = merge;
        buildTree(datas, pKey, key, rootFlag);
    }





    public boolean add(T data){
        V nodeKey = key.apply(data);
        V nodePKey = pKey.apply(data);
        //1.是根节点
        if (nodeKey == rootFlag){
            root .setData(merge.apply(data, root.getData()));
            return true;
        }
        //2.非根节点
        if (!nodes.containsKey(nodePKey)){
            return false;
        }
        TreeNode<T, V> parentNode = nodes.get(nodePKey);
        if (nodes.containsKey(nodeKey)){
            //尚处原来父节点中的记录
            TreeNode<T, V> oldNode = nodes.get(nodeKey);
            if (oldNode != parentNode){
                oldNode.getParentNode().getSubNodes().remove(oldNode);
                parentNode.add(oldNode);
            }

            oldNode.setParentNode(parentNode);
            oldNode.setpKey(nodePKey);
            oldNode.setData(merge.apply(data, oldNode.getData()));

        }else {
            TreeNode<T, V> node = new TreeNode<>(key.apply(data), pKey.apply(data), data);
            node.setParentNode(parentNode);
            parentNode.add(node);
        }
        return true;
    }

    public T get(V key){
        if (!nodes.containsKey(key)){
            return null;
        }
        return nodes.get(key).getData();
    }

    public Boolean delete(V key){
        return delete(key, null);
    }

    public boolean delete(V key, BiFunction<T, T, T> deleteFunction){
        if (key == root){
            return false;
        }
        if (nodes.containsKey(key)){
            TreeNode<T, V> deleteNode = nodes.get(key);
            TreeNode<T, V> parentNode = deleteNode.getParentNode();
            parentNode.getSubNodes().remove(deleteNode);
            List<TreeNode<T, V>> subNodes = deleteNode.getSubNodes();
            for (TreeNode<T, V> subNode : subNodes) {
                subNode.setpKey(deleteNode.pKey);
                subNode.setParentNode(parentNode);
                if (deleteFunction != null){
                    deleteFunction.apply(parentNode.getData(), subNode.getData());
                }
            }
            parentNode.getSubNodes().addAll(subNodes);
        }
        return true;
    }


    /**
     * 构建树
     * @param data 构建成树的数据
     * @param pKey 获取父节点标识
     * @param key 获取获取当前节点标识
     */
    private boolean buildTree(Collection<T> datas, Function<T,V> pKey, Function<T,V> key , V rootFlag) {
        boolean isSuccess = true;
        List<TreeNode<T,V>> treeNodes = new LinkedList<>();
        //构建成
        for (T data : datas) {
            TreeNode<T, V> node = new TreeNode<>(key.apply(data), pKey.apply(data), data);
            treeNodes.add(node);
        }
        Map<V, TreeNode<T, V>> key2Nodes = treeNodes.stream().collect(Collectors.toMap(node -> node.key, Function.identity()));
        for (TreeNode<T, V> node : treeNodes) {
            if (rootFlag.equals(node.key)){
                if (root != null){
                    throw new RuntimeException("构建树时 存在多个根节点");
                }
                root = node;
                nodes.put(node.key, node);
            }

            if (key2Nodes.containsKey(node.pKey)){
                nodes.put(node.key, node);
                key2Nodes.get(node.pKey).add(node);
            }else {
                //如果当前节点是根节点 则说明无法挂靠在这颗树上面
                if (!rootFlag.equals(node.key)){
                    notInTreeNodes.add(node);
                    isSuccess = false;
                }
            }
        }
        //将未挂靠节点的子节点也放在其中
        notInTreeNodes = notInTreeNodes.stream()
                .flatMap(
                        node -> {
                            List<TreeNode<T, V>> allChildNodes = getAllChildNodes(node);
                            allChildNodes.add(node);
                            return allChildNodes.stream();})
                .collect(Collectors.toList());

        return isSuccess;
    }


    /**
     * 获取当前节点的子节点
     * @param node
     * @return
     */
    private List<TreeNode<T,V>> getAllChildNodes(TreeNode<T,V> node){
        List<TreeNode<T,V>> childNodes = new LinkedList<>();
        if (node.getSubNodes() != null && !node.getSubNodes().isEmpty()){
            for (TreeNode<T, V> subNode : node.getSubNodes()) {
                getAllChildNodes(subNode, childNodes);
            }
        }
        return childNodes;
    }

    private void getAllChildNodes(TreeNode<T,V> node, List<TreeNode<T,V>> childNodes){
        if (node.getSubNodes() != null && !node.getSubNodes().isEmpty()){
            for (TreeNode<T, V> subNode : node.getSubNodes()) {
                childNodes.add(subNode);
                getAllChildNodes(subNode, childNodes);
            }
        }
    }

}
