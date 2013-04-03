/**
 * Copyright 2007 Kevin Twidle, Imperial College, London, England.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
 * Boston, MA 02110-1301 USA
 *
 * Contact: Kevin Twidle <kpt@doc.ic.ac.uk>
 *
 * Created on Mar 11, 2007
 *
 * $Log:$
 */

package eu.novi.ponder2.apt;

import static java.util.Collections.emptySet;
import static java.util.Collections.unmodifiableCollection;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import eu.novi.ponder2.apt.Ponder2Processor;
import eu.novi.ponder2.apt.Ponder2notify;
import eu.novi.ponder2.apt.Ponder2op;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

/**
 * This class is used to run an annotation processor that lists class names. The
 * functionality of the processor is analogous to the ListClass doclet in the
 * Doclet Overview.
 * 
 * @author Kevin Twidle
 * @version $Id:$
 */
public class Ponder2Factory implements AnnotationProcessorFactory {

	// Process set of annotations
	private static final Collection<String> supportedAnnotations = unmodifiableCollection(Arrays
			.asList(Ponder2op.class.getName(), Ponder2notify.class.getName()));

	// No supported options
	private static final Collection<String> supportedOptions = emptySet();

	public Collection<String> supportedAnnotationTypes() {
		return supportedAnnotations;
	}

	public Collection<String> supportedOptions() {
		return supportedOptions;
	}

	public AnnotationProcessor getProcessorFor(
			Set<AnnotationTypeDeclaration> atds,
			AnnotationProcessorEnvironment env) {

		return new Ponder2Processor(env);
	}

}
