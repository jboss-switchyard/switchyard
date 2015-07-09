/*
 * Copyright 2015 Red Hat Inc. and/or its affiliates and other contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,  
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.switchyard.component.resteasy.config.model.v2;

import org.switchyard.component.resteasy.config.model.RESTEasyNameValueModel;
import org.switchyard.component.resteasy.config.model.RESTEasyNameValueModel.RESTEasyName;
import org.switchyard.component.resteasy.config.model.SSLContextModel;
import org.switchyard.component.resteasy.config.model.v1.V1RESTEasyNameValueModel;
import org.switchyard.config.Configuration;
import org.switchyard.config.model.Descriptor;
import org.switchyard.config.model.BaseModel;

/**
 * An SSL Config trust Model version 2.
 */
public class V2SSLContextModel extends BaseModel implements SSLContextModel {

    private static final String[] MODEL_CHILDREN_ORDER = new String[]{
        RESTEasyName.verifier.name(),
        RESTEasyName.keystore.name(),
        RESTEasyName.keystorePass.name(),
        RESTEasyName.truststore.name(),
        RESTEasyName.truststorePass.name()
    };

    private RESTEasyNameValueModel _verifier;
    private RESTEasyNameValueModel _keystore;
    private RESTEasyNameValueModel _keystorePass;
    private RESTEasyNameValueModel _truststore;
    private RESTEasyNameValueModel _truststorePass;
    /**
     * Creates a new SSLContextModel.
     * @param namespace namespace
     */
    public V2SSLContextModel(String namespace) {
        super(namespace, RESTEasyName.ssl.name());
        setModelChildrenOrder(MODEL_CHILDREN_ORDER);
    }

    /**
     * Creates a new SSLContextModel.
     * @param namespace namespace
     * @param name the name of the model
     */
    public V2SSLContextModel(String namespace, String name) {
        super(namespace, name);
        setModelChildrenOrder(MODEL_CHILDREN_ORDER);
    }

    /**
     * Creates a new SSLContextModel with the specified configuration and descriptor.
     * @param config the configuration
     * @param desc the descriptor
     */
    public V2SSLContextModel(Configuration config, Descriptor desc) {
        super(config, desc);
        setModelChildrenOrder(MODEL_CHILDREN_ORDER);
    }

    /**
     * {@inheritDoc}
     */
    public String getVerifier() {
        if (_verifier == null) {
            _verifier = getNameValue(RESTEasyName.verifier);
        }
        return _verifier != null ? _verifier.getValue() : null;
    }

    /**
     * {@inheritDoc}
     */
    public SSLContextModel setVerifier(String verifier) {
        _verifier = setNameValue(_verifier, RESTEasyName.verifier, verifier);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String getKeystore() {
        if (_keystore == null) {
            _keystore = getNameValue(RESTEasyName.keystore);
        }
        return _keystore != null ? _keystore.getValue() : null;
    }

    /**
     * {@inheritDoc}
     */
    public SSLContextModel setKeystore(String keystore) {
        _keystore = setNameValue(_keystore, RESTEasyName.keystore, keystore);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String getKeystorePass() {
        if (_keystorePass == null) {
            _keystorePass = getNameValue(RESTEasyName.keystorePass);
        }
        return _keystorePass != null ? _keystorePass.getValue() : null;
    }

    /**
     * {@inheritDoc}
     */
    public SSLContextModel setKeystorePass(String keystorePass) {
        _keystorePass = setNameValue(_keystorePass, RESTEasyName.keystorePass, keystorePass);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String getTruststore() {
        if (_truststore == null) {
            _truststore = getNameValue(RESTEasyName.truststore);
        }
        return _truststore != null ? _truststore.getValue() : null;
    }

    /**
     * {@inheritDoc}
     */
    public SSLContextModel setTruststore(String truststore) {
        _truststore = setNameValue(_truststore, RESTEasyName.truststore, truststore);
        return this;
    }

    /**
     * {@inheritDoc}
     */
    public String getTruststorePass() {
        if (_truststorePass == null) {
            _truststorePass = getNameValue(RESTEasyName.truststorePass);
        }
        return _truststorePass != null ? _truststorePass.getValue() : null;
    }

    /**
     * {@inheritDoc}
     */
    public SSLContextModel setTruststorePass(String truststorePass) {
        _truststorePass = setNameValue(_truststorePass, RESTEasyName.truststorePass, truststorePass);
        return this;
    }

    protected RESTEasyNameValueModel getNameValue(RESTEasyName name) {
        return (RESTEasyNameValueModel)getFirstChildModel(name.name());
    }

    protected RESTEasyNameValueModel setNameValue(RESTEasyNameValueModel model, RESTEasyName name, String value) {
        if (value != null) {
            if (model == null) {
                model = getNameValue(name);
            }
            if (model == null) {
                model = new V1RESTEasyNameValueModel(getNamespaceURI(), name);
                setChildModel(model);
            }
            model.setValue(value);
        } else {
            getModelConfiguration().removeChildren(name.name());
            model = null;
        }
        return model;
    }

}
