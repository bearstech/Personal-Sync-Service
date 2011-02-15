
import sys, getpass

import pycurl
import cStringIO

import vobject

def retr_contacts_from_url(user, passwd, url):
    b = cStringIO.StringIO()

    c = pycurl.Curl()
    c.setopt(pycurl.URL, url)
    c.setopt(pycurl.WRITEFUNCTION, b.write)
    c.setopt(pycurl.SSL_VERIFYHOST, 0)
    c.setopt(pycurl.SSL_VERIFYPEER, 0)
#c.setopt(pycurl.SSL_VERIFYRESULT, 0)
    c.setopt(pycurl.FOLLOWLOCATION, 1)
    c.setopt(pycurl.USERPWD, user+":"+passwd)
    c.perform()

    return b.getvalue()

def retr_contacts_from_file(path):
    try:
        f = open(path, 'r')
        return "".join(f.readlines())
    finally:
        f.close()

def get_contacts_from_vcf(vcf_string):

    contacts_vcf_list = vobject.readComponents( vcf_string )
    contacts_list = []

    i = 0
    for contact in contacts_vcf_list:
        contact_d = {
                    "user_id" : str(i),
                    "firstname" : contact.n.value.given,
                    "lastname" : contact.n.value.family,
                    "nickname" : contact.nickname.value,
                    "status" : "ours",
                    "address" :  { "home" : { "street"  : contact.adr.value }, 
                                   "work" : { "street"  : "40, passage des panoramas",
                                              "city"    : "Paris",
                                              "postcode": "75002",
                                              "country" : "France" }
                              },
                    "email" : { "work" : contact.email.value},
                    "orga" :  {"work": { "name": contact.org.value, 
                                         "role": contact.role.value }
                             },
                }

        contact_d["phone"] = {}
        for tel in contact.tel_list:
            if tel.params['TYPE'] == ['CELL']:
                contact_d["phone"].update({'cell' : tel.value})
            elif tel.params['TYPE'] == ['HOME']:
                contact_d["phone"].update({'home' : tel.value})
            elif tel.params['TYPE'] == ['VOICE']:
                contact_d["phone"].update({'voip' : tel.value})

        contacts_list.append(contact_d)

        i = i + 1

    return contacts_list


def update_vcf_to_db(db_name, vcf_list):
    def get_user_pass(context=""):
        if context == "":
            print "Enter your username: ",
        else:
            print "enter your username "+context+": ",
        t_user = sys.stdin.readline()[:-1]
        t_pass = getpass.getpass()
        return (t_user, t_pass)

    contact_list = get_contacts_from_vcf(vcf_list)
    from personalSyncServer import ContactsDatabase
    db = ContactsDatabase(db_name)
    (s_user, s_pass) = get_user_pass("for your sync server account")
    ab = db.get_address_book(s_user, s_pass)
    if ab is None:
        db.add_user(s_user, s_pass)
        ab = db.get_address_book(s_user, s_pass)
        ab.update_contacts(contact_list)
        print ab.get_contacts(filter_keys=[])
    else:
        ab.update_contacts(contact_list)

if __name__ == "__main__":
    if len(sys.argv) != 2:
        print "usage: "+sys.argv[0]+" file_to_import.vcf"
    else:
        update_vcf_to_db("sync_server_contacts.db", "".join(open(sys.argv[1],'r').readlines()))
