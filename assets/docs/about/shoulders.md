<!--~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
  ~ Copyright © 2014 Reactific Software LLC                                                                           ~
  ~                                                                                                                   ~
  ~ This file is part of Scrupal, an Opinionated Web Application Framework.                                           ~
  ~                                                                                                                   ~
  ~ Scrupal is free software: you can redistribute it and/or modify it under the terms                                ~
  ~ of the GNU General Public License as published by the Free Software Foundation,                                   ~
  ~ either version 3 of the License, or (at your option) any later version.                                           ~
  ~                                                                                                                   ~
  ~ Scrupal is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;                              ~
  ~ without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.                         ~
  ~ See the GNU General Public License for more details.                                                              ~
  ~                                                                                                                   ~
  ~ You should have received a copy of the GNU General Public License along with Scrupal.                             ~
  ~ If not, see either: http://www.gnu.org/licenses or http://opensource.org/licenses/GPL-3.0.                        ~
  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~-->

# Standing On The Shoulders Of Giants
Scrupal would not be possible without the free/libre/open source software community. Any recognition it
achieves clearly needs to acknowledge the technologies upon which it is built, and the dedication and brilliance
of their inventors. This page is all about giving such recognition and making sure we honor their software license
agreement through inclusion of their licenses here.

<tabset>
<tab heading="Java" active><div class="well"><div ng-include="'java.html'"></div></div></tab>
<tab heading="Scala"><div class="well"><div ng-include="'scala.html'"></div></div></tab>
<tab heading="Play"><div class="well"><div ng-include="'play.html'"></div></div></tab>
<tab heading="Specs2"><div class="well"><div ng-include="'specs2.html'"></div></div></tab>
<tab heading="Slick"><div class="well"><div ng-include="'slick.html'"></div></div></tab>
<tab heading="H2"><div class="well"><div ng-include="'h2.html'"></div></div></tab>
<tab heading="Postgresql"><div class="well"><div ng-include="'postgresql.html'"></div></div></tab>
<tab heading="MySQL"><div class="well"><div ng-include="'mysql.html'"></div></div></tab>
<tab heading="SQLite"><div class="well"><div ng-include="'sqlite.html'"></div></div></tab>
<tab heading="AngularJS"><div class="well"><div ng-include="'angular.html'"></div></div></tab>
<tab heading="Bootstrap"><div class="well"><div ng-include="'bootstrap.html'"></div></div></tab>
<tab heading="PBKDF2"><div class="well"><div ng-include="'pbkdf2.html'"></div></div></tab>
<tab heading="BCrypt"><div class="well"><div ng-include="'bcrypt.html'"></div></div></tab>
<tab heading="SCrypt"><div class="well"><div ng-include="'scrypt.html'"></div></div></tab>
</tabset>

<script type="text/ng-template" id="java.html">
<section id="java">
<h3>The Java Virtual Machine and Java Programming Language</h3>
<p>Scrupal runs on top of the long trusted, production competent Java Virtual Machine. Running application software
this way has made sense for numerous organizations and will continue to do so for many years to come. The depth of
resources, wealth of libraries, and deployment flexibility in the JVM eco-system bring levels of competence and
capability Scrupal could not achieve otherwise. Because Scrupal does not re-distribute the JVM, we do not have a
license restriction. However, if you plan to use the "Commerical Features" provided by the JVM via Scrupal then you
should be aware of this clause of the Oracle BCL:</p>
```
Use of the Commercial Features for any commercial or production purpose requires a separate license from Oracle.
“Commercial Features” means those features identified Table 1-1 (Commercial Features In Java SE Product Editions) of
the Java SE documentation accessible at
<a href="http://www.oracle.com/technetwork/java/javase/documentation/index.html">
http://www.oracle.com/technetwork/java/javase/documentation/index.html>
</a>
```
<a href="http://www.oracle.com/technetwork/java/javase/terms/license/index.html">License</a>
</section>
</script>

<script type="text/ng-template" id="scala.html">
<section id="scala">
<h3>Scala Programming Language</h3>
<marked>
Almost all of Scrupal is written in Scala. The initial letters of Scrupal's name are attributed to Scala or at least
it aims to have the same concern: scalability. We tip our hats to all those who have made Scala into such a beautiful
language to work with. The Scala Programming Language requires the following copyright notices to be reproduced:
```
Copyright (c) 2002-2013 EPFL
Copyright (c) 2011-2013 Typesafe, Inc.

All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
   disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
3. Neither the name of the EPFL nor the names of its contributors may be used to endorse or promote products derived
   from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```
[Scala License](http://www.scala-lang.org/license.html)
</marked>

</section>
</script>
<script type="text/ng-template" id="play.html">
<section id="play">
<h3>Play! Framework</h3>
<marked>
The Play framework is at the heart of Scrupal. It provides the basic reactive, scalable, asynchronous, REST-friendly
architecture that is at the crux of what Scrupal is. Play! made building Scrupal a breeze because so much of Scrupal
was already built!  Play requires is Licensed under the Apache License, Version 2.0 under this copyright:
This software is licensed under the Apache 2 license, quoted below.
```
Copyright 2013 Typesafe (http://www.typesafe.com).

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this project except in compliance with
the License. You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0.

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
specific language governing permissions and limitations under the License.
```
[Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0.html)
</marked>
</section>
</script>

  <script type="text/ng-template" id="specs2.html">
<section id="specs2">Specs 2
</section>
</script>

<script type="text/ng-template" id="slick.html">
<section id="slick">
<h3>Slick</h3>
<marked>
The excellent Slick library for fully language-integrated relational programming via JDBC requires the following
license notice to be reproduced
```
Copyright 2011-2012 Typesafe, Inc.

All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

    * Redistributions of source code must retain the above copyright notice,
      this list of conditions and the following disclaimer.

    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.
```
</marked>
</section>
</script>

<script type="text/ng-template" id="h2.html">
<section id="h2">H2
</section>
</script>

<script type="text/ng-template" id="postgresql.html">
<section id="postgresql">Postgresql
</section>
</script>

  <script type="text/ng-template" id="mysql.html">
<section id="mysql">MySQL
</section>
</script>

<script type="text/ng-template" id="sqlite.html">
<section id="sqlite">SQLite
</section>
</script>

<script type="text/ng-template" id="angular.html">
<section id="angular">AngularJS
</section>
</script>

<script type="text/ng-template" id="bootstrap.html">
<section id="bootstrap">Bootstrap
</section>
</script>

<script type="text/ng-template" id="pbkdf2.html">
<section id="pbkdf2">
<h3>PBKDF2</h3>
<marked>
The PBKDF2 Scala library uses the following license:
```
Copyright 2013 Nicolas Rémond (@nremond)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
```
[Apache 2 License](http://www.apache.org/licenses/LICENSE-2.0.html)
</marked>
</section>
</script>

<script type="text/ng-template" id="bcrypt.html">
<section id="bcrypt">
<h3>BCrypt</h3>
<marked>
The BCrypt library requires the following notice to be reproduced:
```
Copyright (c) 2002 Johnny Shelley All rights reserved.

Redistribution and use in source and binary forms, with or without modification, are permitted provided that t\he
following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
   disclaimer in the documentation and/or other materials provided with the distribution.
3. Neither the name of the author nor any contributors may be used to endorse or promote products derived from this
   software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY COPYRIGHT HOLDERS AND CONTRIBUTORS ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
```
</marked>
</section>
</script>

<script type="text/ng-template" id="scrypt.html">
<section id="scrypt">
<h3>Scrypt</h3>
<marked>
The Scrypt library uses Attribution-NonCommercial-NoDerivs 3.0 United States (CC BY-NC-ND 3.0 US) and the copyright
notice below is sufficient attribution.
```
Copyright (c) 2013 DCIT, a.s. http://www.dcit.cz / Karel Miko
```
[C BY-NC-ND 3.0 US](http://creativecommons.org/licenses/by-nc-nd/3.0/us/)
</marked>
</section>
</script>

