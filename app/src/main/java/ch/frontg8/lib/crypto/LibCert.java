package ch.frontg8.lib.crypto;

import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.x500.X500Name;
import org.spongycastle.asn1.x509.SubjectPublicKeyInfo;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.X509v1CertificateBuilder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.operator.ContentSigner;
import org.spongycastle.operator.OperatorCreationException;
import org.spongycastle.operator.jcajce.JcaContentSignerBuilder;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by tstauber on 03.11.15.
 */
public class LibCert {
    private static final String BC = BouncyCastleProvider.PROVIDER_NAME;

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static X509Certificate generateCertificate(KeyPair keyPair) throws OperatorCreationException, CertificateException {
        PrivateKey privKey = keyPair.getPrivate();
        PublicKey pubKey = keyPair.getPublic();

        ContentSigner sigGen = new JcaContentSignerBuilder("SHA256withECDSA").setProvider(BC).build(privKey);
        SubjectPublicKeyInfo subPubKeyInfo = new SubjectPublicKeyInfo(ASN1Sequence.getInstance(pubKey.getEncoded()));

        // today
        Calendar date = new GregorianCalendar();
        date.set(Calendar.HOUR_OF_DAY, 0);
        date.set(Calendar.MINUTE, 0);
        date.set(Calendar.SECOND, 0);
        date.set(Calendar.MILLISECOND, 0);

        Date startDate = date.getTime();
        // next year
        date.add(Calendar.YEAR, 1);

        Date endDate = date.getTime();

        X509v1CertificateBuilder v1CertGen = new X509v1CertificateBuilder(
                new X500Name("CN=frontg8"),
                BigInteger.ONE,
                startDate, endDate,
                new X500Name("CN=frontg8"),
                subPubKeyInfo);

        X509CertificateHolder certHolder = v1CertGen.build(sigGen);

        return new JcaX509CertificateConverter().setProvider(BC).getCertificate(certHolder);
    }
}
