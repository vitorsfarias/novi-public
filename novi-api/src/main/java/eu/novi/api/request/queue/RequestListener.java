package eu.novi.api.request.queue;

/**
 * 
 * Copyright (c) 2012, NOVI Consortium, European FP7 NOVI Project.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *  * Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *  * Neither the name of the NOVI Consortium nor the
 *    names of its contributors may be used to endorse or promote products
 *    derived from this software without specific prior written permission.
 *    
 *    
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL NOVI CONSORTIUM BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * 
 ******************************************************************************************
 * This is the interface that NOVI-API provides to other services in NOVI-API layer.
 * If they are interested in processing a request, they should :
 * 
 * 1. Provide a request processor in their component
 * 2. Reference a requestListener service provided by NOVI API
 * 3. Add their requestProcessor to the requestListener service
 * 
 * Whenever a request arrive, their request processor will be invoked 
 * by request Listener.
 * 
 * 
 *  @author <a href="mailto:a.wibisono@uva.nl">Adianto Wibisono</a>
 *  
 *******************************************************************************************/
public interface RequestListener {

		public void addRequestProcessor(RequestProcessor processor);
		public void addDeleteRequestProcessor(DeleteRequestProcessor deletePRocessor);

}
