# AIA4
AIA4 is a drastic redesign of the AIA Lang. It reverts back to the simplicity of AIA 1. **NOTE** Development on AIA4 has ceised since we have joint forces with the JERAN FOUNDATION to work on SIA and the VERA Project, this decision was made by new manangement we decided to move away from our old projects. We sincerely hope that someone can find a use for this!

# Source
AIA4 goes back to more assembly like code found in AIA1. Source files
are commonly stored as .aia or .aia4. Sometimes it is also stored as .asm,
this is not recommended, since it is not a *real* assembler and could cause confusion.
The following is an example of a program counting to 0 to 100.
```asm
@lnk_dat "counter.aiad"

log "This is an example program counting up to 100\n"

set $0 0x00 ; counter
set $1 0x01 ; incrementor
set $2 0x65 ; limit

:loop
   log "I: "
   log $0
   log "\n"

   add $0 $1 ; Add the values from addresses $0 and $1 together
   cpr $1 $0 ; Copy the value of the accumulator to address $0

   cmp $0 $2 ; Check the counter against the limit
   jl  _loop ; Jump back to the start of the loop if $0 is smaller than 0x65 (0x4d)

   log "Reached 100!\n"
   stop      ; Stops the program, this is only reached if $0 exceeds 0x64
```

# Compilation
The AIA4 runtime has a built in Compiler. This Compiler does no code-optimization, rather it translates the source
1 to 1 into binary.
This compiler is not finished as of right now.

# Runtime
The AIA4 runtime is easy to use. To execute a program simply execute the runtime jar.
This requires you to put atleast the `-f <compiled file>` argument. There are also these
other arguments:
* -nis              | Enable the Netowrk Instruction Set (Not Implemented)
* -ms <int>         | Set the amount of bytes usable as memory
* -v                | Gives the current version
* -saveState <path> | Save all data of the LangProcessor to a text file.
* -pje				| Enables the printing of Java Exceptions in the LangProcessor
* -debug            | Enables program debug mode
* -sl               | Enables the SystemLogger

# Big TODO / WIP Things
* Implement Network Instruction Set
* Finish STDIO Instruction Execution
* Fix Issue with Label Offset in Compiler
	
# License
BSD 3-Clause License

Copyright (c) 2021, GansGruppe
All rights reserved.

Redistribution and use in source and binary forms, with or without<br>
modification, are permitted provided that the following conditions are met:<br>

1. Redistributions of source code must retain the above copyright notice, this<br>
   list of conditions and the following disclaimer.<br>

2. Redistributions in binary form must reproduce the above copyright notice,<br>
   this list of conditions and the following disclaimer in the documentation<br>
   and/or other materials provided with the distribution.<br>

3. Neither the name of the copyright holder nor the names of its<br>
   contributors may be used to endorse or promote products derived from<br>
   this software without specific prior written permission.<br>

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"<br>
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE<br>
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE<br>
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE<br>
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL<br>
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR<br>
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER<br>
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,<br>
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE<br>
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.<br>

# Owners & Authors
## Owners
* Alan Goose : Member of GansGruppe/Teiwaz
* Henry Portsmith : Member of GansGruppe/Teiwaz
* Andreas Hammer: Member of GansGruppe/Teiwaz

## Authors
* Alan Goose
* Felix Eckert
