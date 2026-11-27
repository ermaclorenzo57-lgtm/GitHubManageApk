import unittest
from verify_apk_signer import verify

class VerifySignerTest(unittest.TestCase):
    def test_formats(self):
        d = "a" * 64
        for prefix in ("Signer #1 certificate", "V2 Signer: certificate", "V3.1 Signer: certificate"):
            self.assertTrue(verify(f"Verifies\nNumber of signers: 1\n{prefix} SHA-256 digest: {d}\n", d))
    def test_fail_closed(self):
        d = "a" * 64
        for report in ("Number of signers: 1", f"Number of signers: 2\nV2 Signer: certificate SHA-256 digest: {d}\n", f"Number of signers: 1\nV2 Signer: certificate SHA-256 digest: {'b'*64}\n", f"Number of signers: 1\nV2 Signer: certificate SHA-256 digest: {d}\nV3 Signer: certificate SHA-256 digest: {'b'*64}\n"):
            with self.assertRaises(ValueError): verify(report,d)

if __name__ == "__main__":
    unittest.main()
