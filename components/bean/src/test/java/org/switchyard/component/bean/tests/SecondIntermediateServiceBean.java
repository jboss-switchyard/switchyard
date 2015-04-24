package org.switchyard.component.bean.tests;

import javax.inject.Inject;

import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.ReferenceInvoker;
import org.switchyard.component.bean.Service;

@Service(name="SecondIntermediateService")
public class SecondIntermediateServiceBean implements BeanReferenceService {

    @Inject
    @Reference("SecondIntermediateComponent/BeanReferenceService")
    private BeanReferenceService _bean;

    @Inject
    @Reference("SecondIntermediateComponent/BeanReferenceService")
    private ReferenceInvoker _invoker;
    
    @Override
    public String invoke(String input) {
        String out = _bean.invoke(input);
        assert out.indexOf("SecondIntermediate") >= 0: "consumer was not me!? - " + out;
        try {
            out = _invoker.newInvocation().invoke().getMessage().getContent(String.class);
        } catch (Exception e) {
            assert false: e;
        }
        assert out.indexOf("SecondIntermediate") >= 0: "consumer was not me!? - " + out;
        return input + out;
    }

}
