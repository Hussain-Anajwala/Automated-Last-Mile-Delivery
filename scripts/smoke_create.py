"""Live HTTP smoke test for delivery creation (Phase 5).
Usage: python -I scripts/smoke_create.py <file holding the demo password>
The password file is local only and must never be committed.
"""
import http.cookiejar, re, sys, urllib.parse, urllib.request
BASE = "http://localhost:18080"
PW = open(sys.argv[1]).read().strip()

class NoRedirect(urllib.request.HTTPRedirectHandler):
    def redirect_request(self, *a, **k): return None

def session():
    jar = http.cookiejar.CookieJar()
    return urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar), NoRedirect)

def call(op, path, data=None):
    body = urllib.parse.urlencode(data).encode() if data is not None else None
    try:
        r = op.open(urllib.request.Request(path if path.startswith("http") else BASE + path, data=body))
        return r.status, r.headers.get("Location"), r.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.headers.get("Location"), e.read().decode()

def csrf(html): return re.search(r'name="_csrf" value="([^"]+)"', html).group(1)

def login(email):
    op = session()
    _, _, html = call(op, "/login")
    st, loc, _ = call(op, "/login", {"username": email, "password": PW, "_csrf": csrf(html)})
    print(f"login {email}: {st} -> {loc}")
    return op

d = login("dispatcher@lastmile.test")
_, _, form = call(d, "/deliveries/new")
token = csrf(form)
st, loc, _ = call(d, "/deliveries/new", {"_csrf": token, "recipientName": "Meera Fictional", "recipientPhone": "9876543210",
    "deliveryAddress": "12 Sample Lane", "deliveryCity": "Pune", "referenceNumber": "ORD-DEMO-1", "assignedAgentId": "",
    "status": "DELIVERED", "deliveryCode": "HACK"})
print(f"valid create: {st} -> {loc}")
st, _, html = call(d, loc)
code = re.search(r'data-testid="delivery-code">([^<]+)<', html).group(1)
statusv = re.search(r'data-status="([A-Z_]+)"', html).group(1)
print(f"detail: {st} code={code} status={statusv} flash={'flash-success' in html} history_rows={html.count('data-testid=\"history-entry\"')}")
_, _, form = call(d, "/deliveries/new")
st, loc, html = call(d, "/deliveries/new", {"_csrf": csrf(form), "recipientName": "", "recipientPhone": "12ab",
    "deliveryAddress": "Kept Address", "deliveryCity": "", "referenceNumber": ""})
print(f"invalid create: {st} location={loc} error_summary={'form-error-summary' in html} "
      f"phone_msg={'Enter 7 to 15 digits' in html} input_preserved={'value=\"Kept Address\"' in html}")
a = login("agent.one@lastmile.test")
st, _, _ = call(a, "/deliveries/new"); print(f"agent GET /deliveries/new: {st}")
st, _, _ = call(a, "/deliveries/1"); print(f"agent GET unassigned /deliveries/1: {st}")
bad = session(); _, _, html = call(bad, "/login")
st, loc, _ = call(bad, "/login", {"username": "dispatcher@lastmile.test", "password": "wrong", "_csrf": csrf(html)})
print(f"wrong password: {st} -> {loc}")
