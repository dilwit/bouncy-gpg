package name.neuhalfen.projects.crypto.bouncycastle.openpgp.decrypting;

import name.neuhalfen.projects.crypto.bouncycastle.openpgp.SignatureCheckingMode;
import name.neuhalfen.projects.crypto.bouncycastle.openpgp.testtooling.Configs;
import org.bouncycastle.util.io.Streams;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;

import static name.neuhalfen.projects.crypto.bouncycastle.openpgp.testtooling.ExampleMessages.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.mockito.Mockito.*;

/**
 * All decryption schemes must adhere to the same basic rules regarding crypto.
 */
public abstract class DecryptBaseTest {

    @Test
    public void decrypting_anyData_doesNotCloseInputStream() throws IOException, SignatureException, NoSuchAlgorithmException {

        final DecryptWithOpenPGPInputStreamFactory sut = DecryptWithOpenPGPInputStreamFactory.create(Configs.buildConfigForDecryptionFromResources());

        InputStream in = spy(new ByteArrayInputStream(IMPORTANT_QUOTE_SIGNED_COMPRESSED.getBytes("US-ASCII")));

        final InputStream decryptAndVerify = sut.wrapWithDecryptAndVerify(in);
        decryptAndVerify.close();

        verify(in, never()).close();
    }


    @Test
    public void decryptingAndVerifying_smallAmountsOfData_correctlyDecryptsUncompressedAndArmored() throws IOException, SignatureException, NoSuchAlgorithmException {
        final DecryptionConfig config = Configs.buildConfigForDecryptionFromResources();

        String decryptedQuote = decrypt(IMPORTANT_QUOTE_SIGNED_NOT_COMPRESSED.getBytes("US-ASCII"), config);
        Assert.assertThat(decryptedQuote, equalTo(IMPORTANT_QUOTE_TEXT));
    }

    @Test
    public void decryptingAndVerifying_smallAmountsOfData_correctlyDecryptsCompressedAndArmored() throws IOException, SignatureException, NoSuchAlgorithmException {

        final DecryptionConfig config = Configs.buildConfigForDecryptionFromResources();

        String decryptedQuote = decrypt(IMPORTANT_QUOTE_SIGNED_COMPRESSED.getBytes("US-ASCII"), config);
        Assert.assertThat(decryptedQuote, equalTo(IMPORTANT_QUOTE_TEXT));
    }

    @Test(expected = IOException.class)
    public void decryptingTamperedSignedCiphertext_fails() throws IOException, NoSuchAlgorithmException {

        final DecryptWithOpenPGPInputStreamFactory sut = DecryptWithOpenPGPInputStreamFactory.create(Configs.buildConfigForDecryptionFromResources());

        byte[] buf = IMPORTANT_QUOTE_SIGNED_NOT_COMPRESSED.getBytes("US-ASCII");

        // tamper
        buf[666]++;

        final InputStream plainTextInputStream = sut.wrapWithDecryptAndVerify(new ByteArrayInputStream(buf));

        Streams.drain(plainTextInputStream);
    }

    @Ignore
    @Test
    public void TODO_testForSpecificReciptien() throws IOException, SignatureException, NoSuchAlgorithmException {
        Assert.assertTrue(false);
    }

    @Test(expected = IOException.class)
    public void decryptingMessage_withoutHavingSecretKey_fails() throws IOException, SignatureException {
        final DecryptionConfig config = Configs.buildConfigForDecryptionFromResources(SignatureCheckingMode.IgnoreSignatures);

        decrypt(IMPORTANT_QUOTE_NOT_ENCRYPTED_TO_ME.getBytes("US-ASCII"), config);
    }

    @Test(expected = IOException.class)
    public void decryptingUnsignedMessage_butAnySignatureIsRequired_fails() throws IOException, SignatureException {
        final DecryptionConfig config = Configs.buildConfigForDecryptionFromResources(SignatureCheckingMode.RequireAnySignature);

        final String decryptedQuote = decrypt(IMPORTANT_QUOTE_NOT_SIGNED_NOT_COMPRESSED.getBytes("US-ASCII"), config);

        Assert.assertThat(decryptedQuote, equalTo(IMPORTANT_QUOTE_TEXT));
    }

    @Test(expected = IOException.class)
    public void decryptingUnsignedMessage_butSpecificSignatureIsRequired_fails() throws IOException, SignatureException {
        final DecryptionConfig config = Configs.buildConfigForDecryptionFromResources(SignatureCheckingMode.RequireSpecificSignature);

        final String decryptedQuote = decrypt(IMPORTANT_QUOTE_NOT_SIGNED_NOT_COMPRESSED.getBytes("US-ASCII"), config);

        Assert.assertThat(decryptedQuote, equalTo(IMPORTANT_QUOTE_TEXT));
    }

    @Test
    public void decryptingUnsignedMessage_butSignatureIsNotRequired_succeeds() throws IOException, SignatureException {
        final DecryptionConfig config = Configs.buildConfigForDecryptionFromResources(SignatureCheckingMode.IgnoreSignatures);

        final String decryptedQuote = decrypt(IMPORTANT_QUOTE_NOT_SIGNED_NOT_COMPRESSED.getBytes("US-ASCII"), config);

        Assert.assertThat(decryptedQuote, equalTo(IMPORTANT_QUOTE_TEXT));
    }

    @Test
    public void decryptingSignedMessage_butSignatureIsNotRequired_succeeds() throws IOException, SignatureException {
        final DecryptionConfig config = Configs.buildConfigForDecryptionFromResources(SignatureCheckingMode.IgnoreSignatures);

        final String decryptedQuote = decrypt(IMPORTANT_QUOTE_SIGNED_COMPRESSED.getBytes("US-ASCII"), config);

        Assert.assertThat(decryptedQuote, equalTo(IMPORTANT_QUOTE_TEXT));
    }

    abstract String decrypt(byte[] encrypted, DecryptionConfig config) throws IOException, SignatureException;
}