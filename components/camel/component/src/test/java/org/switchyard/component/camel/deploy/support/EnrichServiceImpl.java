package org.switchyard.component.camel.deploy.support;

import org.switchyard.component.bean.Service;

@Service(EnrichService.class)
public class EnrichServiceImpl implements EnrichService {

    @Override
    public Integer aaa(Integer id) {
        throw new RuntimeException("invalid execution");
    }

    @Override
    public Integer doNothing(Integer id) {
        return id;
    }

    @Override
    public Integer zzz(Integer id) {
        throw new RuntimeException("invalid execution");
    }

}
