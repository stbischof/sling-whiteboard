<%-- 
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
--%>

<%-- 
N plane schema: Navigation
There's no real need for this to be a JSP script, we might want to
create a "text passthrough" script engine for such things.
--%>

"""
Some fields use this Scalar to provide unstructured or semi-structured data
"""
scalar Object

type Query {
  """ 
  Query a single Folder.
  If not specified, the path defaults to the Resource which receives the query request.
  """
  folder(path: String) : Folder @fetcher(name:"samples/folder")

  """ 
  Paginated query for multiple Folders.
  """
  folders(path: String, limit: Int, after: String) : FolderConnection @connection(for: "Folder") @fetcher(name:"samples/folders")

  """
  Query a single Document. If not specified, the path defaults to the Resource which receives the query request
  """
  document(path : String) : Document @fetcher(name:"samples/document")

  """ 
  Paginated query for multiple documents.
  'lang' indicates the query language - TODO provide a query to list those languages and their documentation
  """
  documents(lang: String, query : String, limit: Int, after: String) : DocumentConnection @connection(for: "Document") @fetcher(name:"samples/documents")
}

"""
A Folder can contain other Folders or Documents
""" 
type Folder {
  path : ID!
  header : ContentItemHeader!
}

"""
Common header for Folders and Documents
""" 
type ContentItemHeader {
  """ path of the parent Folder or Document """
  parent : String

  """ The resource type of this document, can be used for example to select publishing templates """
  resourceType : String

  """ The resource supertype provides a simple form of inheritance, for templating fallbacks for example """
  resourceSuperType : String

  """ The title of this document, if supplied """
  title : String

  """ The description of this document, if supplied """
  description : String

  """ The summary of this document, if supplied """
  summary : String
  links : [Link]

  """
  etc can contain any additional information, as an unstructured Object scalar
  """
  etc : Object
}

"""
A Document represents content that's usually meant for authoring and publishing
"""
type Document {
  path : ID!
  header : ContentItemHeader!
  backstage : Backstage

  """ 
  The document's body is unstructured, usually generated by a document aggregator service.
  Fields like 'resourceType' in that content can help rendering or applying UI logic to it.
  """
  body : Object
}

"""
Backstage is for data related to authoring or publishing content:
authoring rules, publishing hints etc.
"""

type Backstage {
  authoring : Object
  publishing : Object

  """
  etc can contain any additional information, as an unstructured Object scalar
  """
  etc : Object
}

"""
A link with its relationship and href
""" 
type Link {
  """
  The link relationship
  TODO defined standard values for this
  """
  rel: String

  """
  The link's href, as would be used in HTML
  """
  href: String!
}

"""
Commands can be sent using this Mutation
"""
type Mutation {
  """ 
  'lang' is the command language - TODO provide a query that lists languages with their help text
  'script' is the script to execute, in the language indicated by 'lang'
  """  
  command(lang: String, script: String) : CommandResult @fetcher(name:"samples/command")
}

"""
The result of executing a command
"""
type CommandResult {
  """ true if the command was successful (TODO use status/error codes?) """
  success: Boolean!

  """ The command output, as text """
  output: String

  """ Optional help text for this command """
  help: String

  """ 
  Links can point to resources that the command created, for example,
  or to the documentation of the command itself, or its language.
  """
  links: [Link]
}