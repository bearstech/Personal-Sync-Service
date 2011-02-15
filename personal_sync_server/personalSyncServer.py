from sqlite3 import *
from bottle import route, run, request, abort, debug
import simplejson

from datetime import datetime
import os

# JSON format:
#  'handle'       'u'
#  'firstname'    'f'
#  'lastname'     'l'
#  'status'       's'
#  'phone_home'   'h'
#  'phone_office' 'o'
#  'phone_mobile' 'm'
#  'email'        'e'
ours_list = []

class AddressBook:
    def __init__(self, db, ident):
        self._conn = db
        self._id = ident

    def __get_contacts(self):
        try:
            curs = self._conn.cursor()
            curs.execute("""SELECT * 
                              FROM login l 
                             INNER JOIN contact c ON l.id = c.owner_id
                             WHERE l.id = ?""", (self._id,))
            contacts = curs.fetchall()
            for contact in contacts:
                curs.execute("""SELECT type, name, role
                                FROM organization
                                WHERE user_id = ?""", (contact['user_id'],))
                contact['orga'] = curs.fetchall()
                curs.execute("""SELECT phonetype, phone
                                FROM phonenumber
                                WHERE user_id = ?""", (contact['user_id'],))
                contact['phone'] = curs.fetchall()
                curs.execute("""SELECT emailtype, email
                                FROM email
                                WHERE user_id = ?""", (contact['user_id'],))
                contact['email'] = curs.fetchall()
                curs.execute("""SELECT addrtype, 
                                 FROM address
                                WHERE user_id = ?""", (contact['user_id'],))
                contact['address'] = curs.fetchall()
                curs.execute("""SELECT IMtype, IMhandler
                                FROM instantmessenging
                                WHERE user_id = ?""", (contact['user_id'],))
                contact['im'] = curs.fetchall()

            return contacts
        finally:
            curs.close()

    def get_contacts(self, filter_keys=[]):
        return [dict(filter(lambda (k,v): k in filter_keys or len(filter_keys) is 0, c.iteritems())) for c in self.__get_contacts()]

    def get_updates(self, timestamp, filter_keys=[]):
        if type(timestamp) is str:
            timestamp = datetime.strptime(timestamp,'%Y/%m/%d %H:%M')
        #print self.__get_contacts()
        return [dict(filter(lambda (k,v): k in filter_keys or len(filter_keys) is 0, c.iteritems())) for c in self.__get_contacts() if datetime.strptime(c['timestamp'],'%Y/%m/%d %H:%M') <= timestamp]


    def get_contact(self, name):
        db_list = []
        contacts = []
        # TODO
        for contact in db_list:
            contact_d = {
                        "user_id" : str(i),
                        "firstname" : contact.n.value.given,
                        "lastname" : contact.n.value.family,
                        "email" : contact.email.value,
#                "a" : contact.adr.value,
#                "o" : contact.org.value,
                        "status" : contact.role.value,
                        "nickname" : contact.nickname.value,
                    }
            contacts += [contact_d]
        # XXX
        return contacts
            
    def update_contacts(self, contact_list):
        """
        contact_list shall be a list of dictionaries like this :

        [ { "user_id" : integer,
            "firstname" : str,
            "lastname" : str,
            "middlename" : str,
            "namesuffix" : str,
            "phonetic_given_name" : str,
            "phonetic_middle_name" : str,
            "phonetic_family_name" : str,
            "nickname" : str,
            "website" : str,
            "status" : str,
            "orga" : { str,
            "phone" : { type : number }
            "email" : { type : addr },
            "address" : { type : { "street" : str, "city" : str, "postcode" : str, "country" : str } },
            "IM" : { type : { type : handle } } ]

            where type is usually one of "work", "home" etc.. 
                                         "work", "cell" for phone and 
                                         "aim", "jabber" etc.. for IM
        """
        timestamp = datetime.strftime(datetime.now(),'%Y/%m/%d %H:%M')
        for c in contact_list:
            try:
                print c
                self._conn.cursor().execute("INSERT INTO contact VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)", 
                                                                        (self._id,
                                                                         c.get('user_id',''),
                                                                         timestamp,
                                                                         c.get('firstname',''),
                                                                         c.get('lastname',''),
                                                                         c.get('middlename',''),
                                                                         c.get('namesuffix',''),
                                                                         c.get('phonetic_given_name',''),
                                                                         c.get('phonetic_middle_name',''),
                                                                         c.get('phonetic_family_name',''),
                                                                         c.get('nickname',''),
                                                                         c.get('website',''),
                                                                         c.get('status','')
                                                                        )
                                           )
                
                for k,v in c.iteritems():
                    if k == "phone":
                        for typ,num in v.iteritems():
                            self._conn.cursor().execute("INSERT INTO phonenumber VALUES (?, ?, ?)", (c['user_id'],typ,num))
                    elif k == "orga":
                        for name,role in v.iteritems():
                            self._conn.cursor().execute("INSERT INTO organization VALUES (?, ?, ?)", (c['user_id'],name,role))
                    elif k == "email":
                        for typ,addr in v.iteritems():
                            self._conn.cursor().execute("INSERT INTO email VALUES (?, ?, ?)", (c['user_id'],typ,addr))
                    elif k == "address":
                        for typ,addr in v.iteritems():
                            self._conn.cursor().execute("INSERT INTO address VALUES (?, ?, ?, ?, ?)", (c['user_id'],k,typ,
                                                                                                                addr['street'],
                                                                                                                addr['postcode'],
                                                                                                                addr['city'],
                                                                                                                addr['country']))
                    elif k == "IM":
                        for typ,addr in v.iteritems():
                            self._conn.cursor().execute("INSERT INTO InstantMessenging VALUES (?, ?, ?)", (c['user_id'],typ,addr))

                self._conn.commit()
            except:
                self._conn.rollback()



class ContactsDatabase:
    _schema = '''
CREATE TABLE login (
    id INTEGER primary key,
    login TEXT,
    password TEXT
);

CREATE TABLE contact (
    owner_id INTEGER,
    user_id INTEGER,
    timestamp text,
    firstname text,
    lastname text,
    middlename text,
    namesuffix text,
    phonetic_given_name text,
    phonetic_middle_name text,
    phonetic_family_name text,
    nickname text,
    website text,
    status text
);

CREATE TABLE organization (
    user_id IMTEGER,
    type text,
    name text,
    role text
);

CREATE TABLE email (
    user_id INTEGER,
    emailtype text,
    email text
);

CREATE TABLE phonenumber (
    user_id INTEGER,
    phonetype text,
    phone text
);

CREATE TABLE address (
    user_id INTEGER,
    addrtype,
    street text,
    postcode text,
    city text,
    country text
);

CREATE TABLE InstantMessenging (
    user_id INTEGER,
    IMtype text,
    IMhandler text
);
'''

    def __init__(self, db_name):
        def dict_factory(cursor, row):
            d = {}
            for idx,col in enumerate(cursor.description):
                d[col[0]] = row[idx]
            return d
        
        if not os.path.exists(db_name):
            self.conn = connect(db_name)
            curs = self.conn.cursor()
            curs.executescript(self._schema)
            curs.close()
        else:
            self.conn = connect(db_name)

        self.conn.row_factory = dict_factory

    def add_user(self, login, password):
        self.conn.cursor().execute("INSERT INTO login values (NULL, ?, ?)", (login, password))
        self.conn.commit()

    def get_address_book(self, login, passwd):
        curs = self.conn.cursor()
        curs.execute("SELECT id FROM login WHERE login=? AND password=?", (login,passwd))
        try:
            return AddressBook(self.conn, curs.fetchone()['id'])
        except:
            return None


def toJSON(v):
    return simplejson.dumps(v)

global db
global addressbook
db = None
addressbook = None

@route('/login', method='POST')
@route('/auth', method='POST')
def authentication():
    print 'authentication'
    print [(k, request.POST[k]) for k in request.POST.keys()]
    username = request.POST['username']
    password = request.POST['password']

    ab = db.get_address_book(username, password)
    if not ab is None:
        global addressbook
        addressbook = ab
        return 'OK'
    else:
        abort(401, 'Invalid credentials')

@route('/fetch_friend_updates', method='POST')
def fetch_friend_updates():
    print 'fetch_friend_updates'
    print [(k, request.POST[k]) for k in request.POST.keys()]

    username = request.POST['username']
    password = request.POST['password']
    if 'timestamp' in request.POST:
        timestamp = request.POST['timestamp']
        timestamp = datetime.strptime(timestamp, '%Y/%m/%d %H:%M')
    else:
        timestamp = datetime.strftime(datetime.now(),'%Y/%m/%d %H:%M')

    ab = db.get_address_book(username, password)
    if not ab is None:
        addressbook = ab
        return toJSON(addressbook.get_updates(timestamp,filter_keys=['user_id', 'nickname', 'firstname', 'lastname', 'email', 'status', 'phone', 'address', 'IM']))
    else:
        abort(401, 'Invalid credentials')

@route('/fetch_status', method='POST')
def fetch_status():
    print 'fetch_status'
    print [(k, request.POST[k]) for k in request.POST.keys()]
    username = request.POST['username']
    password = request.POST['password']

    ab = db.get_address_book(username, password)
    if not ab is None:
        addressbook = ab
        #return toJSON(addressbook.get_contacts(filter_keys=['user_id','status']))
        return toJSON(addressbook.get_contacts(filter_keys=['user_id', 'status']))
    else:
        abort(401, 'Invalid credentials')

def init_service(host_addr='0.0.0.0', host_port=80, db_path="sync_server_contacts.db", dbg=False):
    global db
    db = ContactsDatabase(db_path)
    debug(dbg)
    run(host=host_addr, port=host_port)

if __name__ == '__main__':
    init_service(db_path="sync_server_contacts.db", dbg=True)

