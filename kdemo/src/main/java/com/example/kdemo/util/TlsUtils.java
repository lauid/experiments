package com.example.kdemo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;

@Component
public class TlsUtils {
    private static final Logger logger = LoggerFactory.getLogger(TlsUtils.class);
    
    /**
     * 创建SSL上下文
     */
    public SSLContext createSSLContext(KubernetesSecretUtils.TlsConfig tlsConfig) throws Exception {
        if (tlsConfig == null || !tlsConfig.hasTlsConfig()) {
            logger.debug("No TLS config provided, using default SSL context");
            return SSLContext.getDefault();
        }
        
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            
            if (tlsConfig.isSkipSslVerification()) {
                logger.debug("Creating SSL context with skip verification");
                sslContext.init(null, null, null);
                return sslContext;
            }
            
            // 创建信任管理器
            TrustManagerFactory trustManagerFactory = createTrustManagerFactory(tlsConfig);
            
            // 创建密钥管理器
            KeyManagerFactory keyManagerFactory = createKeyManagerFactory(tlsConfig);
            
            sslContext.init(
                keyManagerFactory != null ? keyManagerFactory.getKeyManagers() : null,
                trustManagerFactory != null ? trustManagerFactory.getTrustManagers() : null,
                null
            );
            
            logger.debug("SSL context created successfully");
            return sslContext;
            
        } catch (Exception e) {
            logger.error("Failed to create SSL context: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 创建信任管理器工厂
     */
    private TrustManagerFactory createTrustManagerFactory(KubernetesSecretUtils.TlsConfig tlsConfig) 
            throws Exception {
        if (tlsConfig.getCaCertificateContent() == null || tlsConfig.getCaCertificateContent().isEmpty()) {
            logger.debug("No CA certificate provided, using default trust manager");
            return null;
        }
        
        try {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) certFactory.generateCertificate(
                new ByteArrayInputStream(tlsConfig.getCaCertificateContent().getBytes())
            );
            
            trustStore.setCertificateEntry("ca", caCert);
            
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm()
            );
            trustManagerFactory.init(trustStore);
            
            logger.debug("Trust manager factory created with CA certificate");
            return trustManagerFactory;
            
        } catch (Exception e) {
            logger.error("Failed to create trust manager factory: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 创建密钥管理器工厂
     */
    private KeyManagerFactory createKeyManagerFactory(KubernetesSecretUtils.TlsConfig tlsConfig) 
            throws Exception {
        if (tlsConfig.getClientCertificateContent() == null || tlsConfig.getClientCertificateContent().isEmpty() ||
            tlsConfig.getClientKeyContent() == null || tlsConfig.getClientKeyContent().isEmpty()) {
            logger.debug("No client certificate or key provided, skipping key manager");
            return null;
        }
        
        try {
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            
            // 加载客户端证书
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            X509Certificate clientCert = (X509Certificate) certFactory.generateCertificate(
                new ByteArrayInputStream(tlsConfig.getClientCertificateContent().getBytes())
            );
            
            // 加载客户端私钥
            String privateKeyPem = tlsConfig.getClientKeyContent()
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                .replace("-----END RSA PRIVATE KEY-----", "")
                .replaceAll("\\s", "");
            
            byte[] privateKeyBytes = Base64.getDecoder().decode(privateKeyPem);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            java.security.PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
            
            // 将证书和私钥添加到密钥库
            keyStore.setKeyEntry("client", privateKey, "".toCharArray(), new X509Certificate[]{clientCert});
            
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm()
            );
            keyManagerFactory.init(keyStore, "".toCharArray());
            
            logger.debug("Key manager factory created with client certificate");
            return keyManagerFactory;
            
        } catch (Exception e) {
            logger.error("Failed to create key manager factory: {}", e.getMessage());
            throw e;
        }
    }
} 