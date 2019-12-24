module.exports = function(RED) {
    function ModuleRequirementsNode(config) {
        RED.nodes.createNode(this,config);
    }
    RED.nodes.registerType("module-requirements", ModuleRequirementsNode);
}