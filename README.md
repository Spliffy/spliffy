spliffy
=======

Spliffy is a versioning file server for the internet, with a file sync tool. It uses hashsplit4j for efficient calculation of file deltas, and these same deltas are persisted in an object database.
Spliffy never stores the same blob twice, regardless of user, repository etc.

You access it by webdav or web browser, and soon we'll add FTP, CardDAV, CalDAV and maybe even DLNA (ohhh!)

!!!! NOTE: Spliffy requires JDK17!!!!

Getting in touch
----------------
Check out the spliffy email list:
https://groups.google.com/forum/#!forum/spliffy-users

Running the Server
------------------
First build the whole spliffy tree with maven (assuming you've checked out to /proj/spliffy), then run jetty in spliffy-server
cd /proj/spliffy
mvn install
cd spliffy-server
mvn jetty:run


File structure
--------------

The top level directory in spliffy is the user. In the current prototype version a user is auto-created called test, so this is accessed on a url like:

http://localhost:8080/user1/ (access either by web browser or webdav)

A user looks a bit like a folder, but it isnt. It can only contain repositories, but repositories can be created just like a normal folder from webdav. 

** A Spliffy Repository is like a Git Repository - when its deleted its deleted permanently!! **

But it doesnt implement Deletable at the moment, so you don't actually have to worry about it (yet)

The test data includes a repository called repo1, although you can add more. Go ahead and open it in your webdav client. Eg:

http://localhost:8080/user1/repo1

Now we can start adding real files and folders. By "real" i mean things that can be versioned and syncronised. Any time you add, delete, or modify that information does not replace what
was there before - its added to it. All that really happens is that we switch a pointer on the repository to the new bytes. At the time of writing the old versions arent actually accessible, but
that will be the first thing to do once this README is written.

File Syncronisation
-------------------
This is the fun stuff. The file sync tool (spliffy-sync) is pretty crude at the moment, just a jar without any GUI, but the plan is to build a decent GUI and have desktop integration etc.

Use it like this:

(java stuff) org.spliffy.sync.SpliffySync /proj/spliffy-test http://localhost:8080/user1/repo1/ user1 password1

Which will sync folder /proj/spliffy-test with a repository at http://localhost:8080/test/a1/

Here's how the file sync tool works:
1. Build a cache of file and directory hashes (using JdbcLocalTripletStore), caching in a H2 database for future use, so the second run is much faster
2. DirWalker walks the directory hashes using JdbcLocalTripletStore and HttpTripletStore looking for differences. When differences are found it walks down to find exactly what resources differ
3. When differences are found DirWalker calls out to SyncingDeltaListener to do something about it
4. SyncingDeltaListener forwards the event to Syncer to make things the same. 
5. If successful it uses JdbcSyncStatusStore to record the fact that the resources are in sync (this information is used for conflict detection)
6. And so on...


How Spliffy syncs individual files
----------------------------------
To upload Syncer uses HashSplit4J to parse the local file, and it uses remote HTTP implementations of BlobStore and HashStore to send the information to the server. Once all the data is sent
it does a PUT to the actual resource, but instead of sending real data it just sends a hash value with a content type of "spliffy/hash". Since the server now has all the blobs and hashes it needs
it just creates a new ItemVersion with the new hash. 

But, to avoid uploading all the blobs over and over again we check if the server already has the blob (identifying it by its hash) with an OPTIONS request. If a file has had a small change most of
the blobs will alreay be on the server. But a file can have 1000's of blobs, and we don't want to do 1000's of OPTIONs requests. So we take advantage of the fact that blobs are immutable and last forever
we cache the existence of remote blobs (with JdbcHashCache) and check that first

To download files we use the local file as a BlobStore (with FileBlobStore). This parses the local file so it knows what blobs it has, then uses that as the primary blob store, and the remote server
as the secondary blob store, and then does a Combiner.combine, with the server acting as a HashStore. Got it?? Its much simpler to code then to explain.




Server Code Architecture
------------------------
We're using the following things:
    - hibernate with JPA annotations (currently user managed transactions)
    - milton for webdav and http
    - freemarker for templating (JSP is sooo 2000-and-late)
    - spring for configuration and wiring up singletons (see src/main/resources/applicationContext.xml)
    - default database is H2, but of course will run on anything hibernate works on
    - jquery for front end stuff

    

Client Code Architecture
------------------------
I've avoided using hibernate on the client, and just used JDBC instead, because users are sensitive to memory consumption by file sync clients, and hibernate eats RAM

Whereever possible we put data to disk instead of holding it in RAM


Server Data Structure
---------------------

org.spliffy.server.db.BaseEntity - base class for users, groups and other security entities

org.spliffy.server.db.BlobHash - identifies the location of a blob. Spliffy is intended
to scale up for BIG repositories, and that means distributed self replicating storage

org.spliffy.server.db.DirectoryMember - this represents the existence of a resource within a directory. identifies the name and hash. A list of these defines a directory

org.spliffy.server.db.RepoVersion - acts as a link between a repository and a root ItemVersion, which defines the entire state of the repository

org.spliffy.server.db.Repository - a container for versioned resources

org.spliffy.server.db.Item - Simply defines the existence of a resource. This will contain the meta data which is immutable across all versions of a resource

org.spliffy.server.db.User - A user

org.spliffy.server.db.FanoutHash - The identifier for the parent of a set of fanouts

org.spliffy.server.db.FanoutEntry - hashes within the fanout

org.spliffy.server.db.ItemVersion - Defines a particular version of some resource. Contains the hash and the type

org.spliffy.server.db.Permission - Will control read and write access to resources, compatible with milton's ACL support

org.spliffy.server.db.Link - provides linked folders. This will primarily be used for sharing folders among users

org.spliffy.server.db.DeletedItem - reference to resources once they're deleted, to make it efficient to bring up a list of deleted items

org.spliffy.server.db.Volume - used for HA distributed storage. A volume is a set of volume instances which replicate data between themselves. To increase
the storage capacity add more Volume's (and more VolumeInstances)

org.spliffy.server.db.VolumeInstance - a physical storage location, such as a hard disk or server.

