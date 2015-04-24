package org.switchyard.component.bean.tests;

import javax.inject.Inject;

import org.switchyard.component.bean.Reference;
import org.switchyard.component.bean.Service;

@Service(name="ForkService")
public class ForkServiceBean implements BeanReferenceService {

    @Inject
    @Reference("FirstIntermediateService")
    private BeanReferenceService _first;

    @Inject
    @Reference("SecondIntermediateService")
    private BeanReferenceService _second;

    @Override
    public String invoke(String input) {
        String out1 = _first.invoke(input);
        String out2 = _second.invoke(input);
        return out1 + out2;
    }

}
