// --- BEGIN COPYRIGHT BLOCK ---
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public
// License as published by the Free Software Foundation;
// version 2.1 of the License.
// 
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
// 
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor,
// Boston, MA  02110-1301  USA 
// 
// Copyright (C) 2007 Red Hat, Inc.
// All rights reserved.
// --- END COPYRIGHT BLOCK ---

#include "nspr.h"
#include <sys/types.h>

#include <stdio.h>
#ifndef XP_WIN32
#include <unistd.h>  /* sleep */
#else /* XP_WIN32 */
#include <windows.h>
#endif /* XP_WIN32 */

#include "main/Base.h"
#include "httpClient/httpc/http.h"
#include "httpClient/httpc/request.h"
#include "httpClient/httpc/response.h"
#include "httpClient/httpc/engine.h"

#include "engine/RA.h"
#include "main/Memory.h"

/*
 * httpSend: sends to an HTTP server
 *   host_port should be in the for "host:port"
 *     e.g. ca.fedora.redhat.com:1027
 *   uri should contain uri including parameter values
 *     e.g. https://ca.fedora.redhat.com:1027/ca/profileSubmitSSLClient?profileId=userKey&screenname=user1&publickey=YWJjMTIzCg
 *   method has to be "GET" or "POST"
 *   body is the HTTP body.  Can have nothing.
 */
PSHttpResponse *httpSend(char *host_port, char *uri, char *method, char *body)
{
    const char* nickname;
    nickname = RA::GetConfigStore()->GetConfigAsString("ra.clientNickname", "");

    char *pPort = NULL;
    char *pPortActual = NULL;


    char hostName[512];

    /*
     * Isolate the host name, account for IPV6 numeric addresses.
     *
     */

    if(host_port)
        strncpy(hostName,host_port,512);

    pPort = hostName;
    while(1)  {
        pPort = strchr(pPort, ':');
        if (pPort) {
            pPortActual = pPort;
            pPort++;
        } else
            break;
    }

    if(pPortActual)
        *pPortActual = '\0';


    /*
    *  Rifle through the values for the host
    */

    PRAddrInfo *ai;
    void *iter;
    PRNetAddr addr;
    int family = PR_AF_INET;

    ai = PR_GetAddrInfoByName(hostName, PR_AF_UNSPEC, PR_AI_ADDRCONFIG);
    if (ai) {
        printf("%s\n", PR_GetCanonNameFromAddrInfo(ai));
        iter = NULL;
        while ((iter = PR_EnumerateAddrInfo(iter, ai, 0, &addr)) != NULL) {
            char buf[512];
            PR_NetAddrToString(&addr, buf, sizeof buf);
            RA::Debug( LL_PER_PDU,
                       "PSHttpResponse::httpSend: ",
                           "Sending addr -- Msg='%s'\n",
                           buf );
            family = PR_NetAddrFamily(&addr);
            RA::Debug( LL_PER_PDU,
                       "PSHttpResponse::httpSend: ",
                           "Sending family -- Msg='%d'\n",
                           family );
            break;
        }
        PR_FreeAddrInfo(ai);
        
    }

    PSHttpServer server(host_port, family);
    server.setSSL(PR_TRUE);
    // use "HTTP10" if no chunking
    PSHttpRequest request( &server, uri, HTTP11, 0 );
    request.setSSL(PR_TRUE);
    request.setCertNickName(nickname);
    request.setMethod(method);
    if (body != NULL)
        request.setBody( strlen(body), body);

    // use with "POST" only
    request.addHeader( "Content-Type", "text/xml" );
    request.addHeader( "Connection", "keep-alive" );
    HttpEngine engine;
    PSHttpResponse *resp =  engine.makeRequest( request, server, 120 /*_timeout*/ , PR_TRUE /* expect chunked*/);

    return resp;
}
