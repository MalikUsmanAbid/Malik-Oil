"""Read-only/stateless checks against the already running local preview. No AI calls."""
import socket
import unittest
import urllib.request
import urllib.error

BASE = 'http://127.0.0.1:8765'


class PreviewHttpSecurity(unittest.TestCase):
    def test_security_headers(self):
        with urllib.request.urlopen(BASE + '/', timeout=12) as response:
            self.assertEqual(response.headers['X-Frame-Options'], 'DENY')
            self.assertEqual(response.headers['Referrer-Policy'], 'no-referrer')
            self.assertEqual(response.headers['Cache-Control'], 'no-store')

    def test_framing_size_and_json_rejections(self):
        for framing, body in [('Content-Length: 2\r\nContent-Length: 2\r\n', b'{}'),
                              ('Transfer-Encoding: chunked\r\nContent-Length: 2\r\n', b'{}'),
                              ('Content-Length: 65537\r\n', b''),
                              ('Content-Length: 1\r\n', b'{')]:
            with self.subTest(framing=framing):
                with socket.create_connection(('127.0.0.1', 8765), timeout=12) as client:
                    headers = 'POST /demo/margin HTTP/1.1\r\nHost: 127.0.0.1:8765\r\nOrigin: ' + BASE + '\r\nContent-Type: application/json\r\n' + framing + '\r\n'
                    client.sendall(headers.encode() + body)
                    self.assertIn(b'400', client.recv(4096).split(b'\r\n')[0])

    def test_vault_cross_origin_rejected(self):
        request = urllib.request.Request(BASE + '/demo/sessions', data=b'{"sessions":[],"revision":"empty"}', headers={'Content-Type': 'application/json', 'Origin': 'https://attacker.invalid'})
        with self.assertRaises(urllib.error.HTTPError) as failure:
            urllib.request.urlopen(request, timeout=12)
        self.assertEqual(failure.exception.code, 403)

    def test_cross_origin_and_wrong_host_rejected(self):
        request = urllib.request.Request(BASE + '/demo/margin', data=b'{}', headers={'Content-Type': 'application/json', 'Origin': 'https://attacker.invalid'})
        with self.assertRaises(urllib.error.HTTPError) as failure:
            urllib.request.urlopen(request, timeout=12)
        self.assertEqual(failure.exception.code, 403)
        request = urllib.request.Request(BASE + '/', headers={'Host': 'attacker.invalid'})
        with self.assertRaises(urllib.error.HTTPError) as failure:
            urllib.request.urlopen(request, timeout=12)
        self.assertEqual(failure.exception.code, 403)


if __name__ == '__main__':
    unittest.main()
