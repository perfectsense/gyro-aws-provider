package gyro.aws.wafv2;

public class WafDefaultAction {

    enum DefaultAction {
        ALLOW,
        BLOCK
    }

    enum RuleAction {
        ALLOW,
        BLOCK,
        COUNT
    }

    enum OverrideAction {
        COUNT,
        NONE
    }
}
