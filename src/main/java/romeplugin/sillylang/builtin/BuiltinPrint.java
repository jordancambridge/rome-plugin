package romeplugin.sillylang.builtin;

import romeplugin.sillylang.types.SillyType;

import java.util.Deque;
import java.util.HashMap;

public class BuiltinPrint extends Builtin {

    @Override
    protected String getId() {
        return "__print";
    }

    @Override
    public void execute(Deque<SillyType> stack, HashMap<String, SillyType> values) {
        System.out.println(stack.pop().getValue());
    }
}
