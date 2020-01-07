module.exports = function(RED) {
    function LoopRequirementsNode(config) {
        RED.nodes.createNode(this,config);
    }
    RED.nodes.registerType("loop-requirements", LoopRequirementsNode);
}