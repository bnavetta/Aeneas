/**
 * Copyright 2015 Benjamin Navetta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bennavetta.aeneas.cli;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Display ASCII tables.
 * A bit like http://nickgravgaard.com/elastictabstops/
 */
public class Table
{
	private List<List<String>> rows = Lists.newArrayList();
	
	private char padChar = ' ';
	private int padAmount = 2;

	public void addRow(List<String> row)
	{
		rows.add(row);
	}
	
	public void setPadAmount(int padAmount)
	{
		this.padAmount = padAmount;
	}
	
	public void setPadChar(char padChar)
	{
		this.padChar = padChar;
	}

	public void addRow(String... row)
	{
		addRow(Arrays.asList(row));
	}

	@Override
	public String toString()
	{
		if(rows.isEmpty())
			return "";


		int numColumns = rows.get(0).size();
		List<Integer> columnWidths = Lists.newArrayList();
		for(int i = 0; i < numColumns; i++)
		{
			columnWidths.add(extractColumn(i).mapToInt(String::length).max().getAsInt());
		}

		return rows.stream().map(row -> {
			List<String> paddedRow = Lists.newArrayListWithCapacity(row.size());
			for(int i = 0; i < row.size(); i++)
			{
				int columnWidth = columnWidths.get(i);
				paddedRow.add(Strings.padEnd(row.get(i), columnWidth, padChar));
			}
			return Joiner.on(Strings.repeat(String.valueOf(padChar), padAmount)).join(paddedRow);
		}).collect(Collectors.joining("\n"));
	}

	private Stream<String> extractColumn(int index)
	{
		return rows.stream().map(l -> l.get(index));
	}
}
