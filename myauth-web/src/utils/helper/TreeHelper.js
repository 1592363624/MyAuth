// 固定列表结构：[{Id:'xxx',ParentId:'xxx'}]
const TreeHelper = {
    getParentIds (id, allNodes) {
        const parents = []
        const theNode = this.findTheNode(id, allNodes)
        if (theNode.ParentId) {
            parents.push(theNode.ParentId)
            parents.push(...this.getParentIds(theNode.ParentId, allNodes))
        }

        return parents
    },
    getChildrenIds (id, allNodes) {
        const childrenIds = []
        const children = allNodes.filter(item => item.ParentId == id)
            .map(item => item.Id)
        if (children.length > 0) {
            childrenIds.push(...children)
            children.forEach(item => {
                childrenIds.push(...this.getChildrenIds(item, allNodes))
            })
        }
        return childrenIds
    },
    findTheNode (id, allNodes) {
        return allNodes.filter(item => item.Id == id)[0]
    }
}

export default TreeHelper
