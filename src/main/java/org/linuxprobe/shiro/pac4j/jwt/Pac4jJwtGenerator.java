package org.linuxprobe.shiro.pac4j.jwt;

import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.encryption.EncryptionConfiguration;
import org.pac4j.jwt.config.signature.SignatureConfiguration;

import java.util.Date;

public class Pac4jJwtGenerator<U extends CommonProfile> implements JwtGenerator<U> {
    private SignatureConfiguration signatureConfiguration;
    private EncryptionConfiguration encryptionConfiguration;

    public Pac4jJwtGenerator(SignatureConfiguration signatureConfiguration, EncryptionConfiguration encryptionConfiguration) {
        this.signatureConfiguration = signatureConfiguration;
        this.encryptionConfiguration = encryptionConfiguration;
    }

    @Override
    public String generate(long expiresIn, U profile) {
        org.pac4j.jwt.profile.JwtGenerator<U> jwtG = new org.pac4j.jwt.profile.JwtGenerator<>(this.signatureConfiguration, this.encryptionConfiguration);
        jwtG.setExpirationTime(new Date(System.currentTimeMillis() + expiresIn * 1000));
        return jwtG.generate(profile);
    }
}
