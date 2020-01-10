/* --- BEGIN COPYRIGHT BLOCK ---
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; version 2 of the License.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * 
 * Copyright (C) 2007 Red Hat, Inc.
 * All rights reserved.
 * --- END COPYRIGHT BLOCK ---
 */

#include "tkstool.h"

PK11SymKey *
TKS_RetrieveSymKey( PK11SlotInfo *slot,
                    char *keyname,
                    void *pwdata )
{
    char       *name       = NULL;
    int         count      = 0;
    int         keys_found = 0;
    PK11SymKey *symKey     = NULL;
    PK11SymKey *nextSymKey = NULL;
    PK11SymKey *rvSymKey   = NULL;

    if( PK11_NeedLogin( /* slot */  slot ) ) {
        PK11_Authenticate( 
        /* slot       */   slot,
        /* load certs */   PR_TRUE,
        /* wincx      */   pwdata );
    }

    /* Initialize the symmetric key list. */
    symKey = PK11_ListFixedKeysInSlot( 
             /* slot     */            slot,
             /* nickname */            NULL,
             /* wincx    */            ( void *) pwdata );

    /* Iterate through the symmetric key list. */
    while( symKey != NULL ) {
        name = PK11_GetSymKeyNickname( /* symmetric key */  symKey );
        if( name != NULL ) {
            if( keyname != NULL ) {
                if( PL_strcmp( keyname, name ) == 0 ) {
                    keys_found++;
                }
            }
        }

        nextSymKey = PK11_GetNextSymKey( /* symmetric key */  symKey );
        PK11_FreeSymKey( /* symmetric key */  symKey );
        symKey = nextSymKey;

        count++;
    }

    /* case 1:  the token is empty */
    if( count == 0 ) {
        /* the specified token is empty */
        rvSymKey = NULL;
        goto retrievedSymKey;
    }

    /* case 2:  the specified key is not on this token */
    if( ( keyname != NULL ) &&
        ( keys_found == 0 ) ) {
        /* the key called "keyname" could not be found */
        rvSymKey = NULL;
        goto retrievedSymKey;
    }

    /* case 3:  the specified key exists more than once on this token */
    if( keys_found != 1 ) {
        /* more than one key called "keyname" was found on this token */
        rvSymKey = NULL;
        goto retrievedSymKey;
    } else {
        /* Re-initialize the symmetric key list. */
        symKey = PK11_ListFixedKeysInSlot( 
                 /* slot     */            slot,
                 /* nickname */            NULL,
                 /* wincx    */            ( void *) pwdata );

        /* Reiterate through the symmetric key list once more, */
        /* this time returning an actual reference to the key. */
        while( symKey != NULL ) {
            name = PK11_GetSymKeyNickname( /* symmetric key */  symKey );
            if( name != NULL ) {
                if( keyname != NULL ) {
                    if( PL_strcmp( keyname, name ) == 0 ) {
                        rvSymKey = symKey;
                        goto retrievedSymKey;
                    }
                }
            }

            nextSymKey = PK11_GetNextSymKey( /* symmetric key */  symKey );
            PK11_FreeSymKey( /* symmetric key */  symKey );
            symKey = nextSymKey;
        }
    }

retrievedSymKey:
    return rvSymKey;
}

