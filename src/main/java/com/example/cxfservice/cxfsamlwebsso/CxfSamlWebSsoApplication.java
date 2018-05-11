package com.example.cxfservice.cxfsamlwebsso;

import java.util.Arrays;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.jaxrs.JAXRSServerFactoryBean;
import org.apache.cxf.rs.security.saml.sso.RequestAssertionConsumerService;
import org.apache.cxf.rs.security.saml.sso.SamlRedirectBindingFilter;
import org.apache.cxf.rs.security.saml.sso.state.EHCacheSPStateManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class CxfSamlWebSsoApplication {

	@Autowired
    private Bus bus;
	@Value("${idpServiceAddress}")
	private String idpServiceAddress;
	@Value("${assertionConsumerServiceAddress}")
	private String assertionConsumerServiceAddress;
 
    public static void main(String[] args) {
        SpringApplication.run(CxfSamlWebSsoApplication.class, args);
    }
  
    @Bean
    public Server rsServer() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
        endpoint.setBus(bus);
        endpoint.setAddress("/app");
        // Register 2 JAX-RS root resources supporting "/sayHello/{id}" and "/sayHello2/{id}" relative paths
        endpoint.setServiceBeans(Arrays.<Object>asList(new HelloServiceImpl()));
        endpoint.setProvider(redirectGetFilter());
        return endpoint.create();
    }
    
    @Bean
    public Server assertionServer() {
        JAXRSServerFactoryBean endpoint = new JAXRSServerFactoryBean();
//        endpoint.setBus(bus);
        endpoint.setAddress("/racs");
        // Register 2 JAX-RS root resources supporting "/sayHello/{id}" and "/sayHello2/{id}" relative paths
        endpoint.setServiceBeans(Arrays.<Object>asList(consumerService()));
//        endpoint.setProvider(redirectGetFilter());
        return endpoint.create();
    }
    
    @Bean
    public SamlRedirectBindingFilter redirectGetFilter() {
    	SamlRedirectBindingFilter filter = new SamlRedirectBindingFilter();
    	filter.setIdpServiceAddress(idpServiceAddress);
    	filter.setAssertionConsumerServiceAddress(assertionConsumerServiceAddress);
    	filter.setStateProvider(stateProvider());
//    	filter.setSignRequest(true);
//    	filter.setSignatureUsername("alias");
//    	filter.setSignaturePropertiesFile("serviceKeystore.properties");
    	return filter;
    }

    @Bean
	public EHCacheSPStateManager stateProvider() {
		return new EHCacheSPStateManager(bus);
	}
    
    @Bean
    public RequestAssertionConsumerService consumerService() {
    	RequestAssertionConsumerService consumerService = new RequestAssertionConsumerService();
    	consumerService.setStateProvider(stateProvider());
    	consumerService.setSupportDeflateEncoding(false);
    	consumerService.setSupportBase64Encoding(false);
    	return consumerService;
    }
}
