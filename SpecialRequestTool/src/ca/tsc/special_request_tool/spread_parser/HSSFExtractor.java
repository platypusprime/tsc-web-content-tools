package ca.tsc.special_request_tool.spread_parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.poi.hssf.eventusermodel.HSSFEventFactory;
import org.apache.poi.hssf.eventusermodel.HSSFListener;
import org.apache.poi.hssf.eventusermodel.HSSFRequest;
import org.apache.poi.hssf.record.BOFRecord;
import org.apache.poi.hssf.record.BoundSheetRecord;
import org.apache.poi.hssf.record.CellValueRecordInterface;
import org.apache.poi.hssf.record.ExtendedFormatRecord;
import org.apache.poi.hssf.record.FontRecord;
import org.apache.poi.hssf.record.LabelRecord;
import org.apache.poi.hssf.record.LabelSSTRecord;
import org.apache.poi.hssf.record.NumberRecord;
import org.apache.poi.hssf.record.Record;
import org.apache.poi.hssf.record.SSTRecord;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

public class HSSFExtractor implements WorkbookExtractor {

	private File file;

	public HSSFExtractor(File file) {
		this.file = file;
	}

	@Override
	public ArrayList<String> extractSheetNames() {

		try {
			FileInputStream fin = new FileInputStream(file);
			POIFSFileSystem poifs = new POIFSFileSystem(fin);
			InputStream din = poifs.createDocumentInputStream("Workbook");

			HSSFRequest req = new HSSFRequest();
			SheetNameListener listener = new SheetNameListener();

			req.addListener(listener, BoundSheetRecord.sid);

			HSSFEventFactory factory = new HSSFEventFactory();
			factory.processEvents(req, din);
			ArrayList<String> sheetNames = listener.getData();

			fin.close();
			din.close();

			return sheetNames;
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<String>();
		}
	}

	@Override
	public ArrayList<SheetData> extractData() {

		try {
			FileInputStream fin = new FileInputStream(file);
			POIFSFileSystem poifs = new POIFSFileSystem(fin);
			InputStream din = poifs.createDocumentInputStream("Workbook");

			HSSFRequest req = new HSSFRequest();
			CellDataListener listener = new CellDataListener();

			// req.addListenerForAllRecords(listener);
			req.addListener(listener, BOFRecord.sid);
			req.addListener(listener, SSTRecord.sid);
			req.addListener(listener, FontRecord.sid);
			req.addListener(listener, ExtendedFormatRecord.sid);
			req.addListener(listener, BoundSheetRecord.sid);
			req.addListener(listener, NumberRecord.sid);
			req.addListener(listener, LabelRecord.sid);
			req.addListener(listener, LabelSSTRecord.sid);

			HSSFEventFactory factory = new HSSFEventFactory();
			factory.processEvents(req, din);
			ArrayList<SheetData> sheetData = listener.getData();

			fin.close();
			din.close();

			return sheetData;
		} catch (IOException e) {
			e.printStackTrace();
			return new ArrayList<SheetData>();
		}
	}

	class SheetNameListener implements HSSFListener {

		private ArrayList<String> sheetNames = new ArrayList<String>();

		public void processRecord(Record record) {
			switch (record.getSid()) {

			case BoundSheetRecord.sid:
				BoundSheetRecord bsr = (BoundSheetRecord) record;
				sheetNames.add(bsr.getSheetname());
				break;
			default:
				break;
			}
		}

		public ArrayList<String> getData() {
			return sheetNames;
		}
	}

	class CellDataListener implements HSSFListener {

		private SSTRecord sstrec;
		private ArrayList<SheetData> sheets = new ArrayList<SheetData>();
		private int curSheetIndex = -1;

		ArrayList<ExtendedFormatRecord> xfRecords = new ArrayList<ExtendedFormatRecord>();
		ArrayList<FontRecord> fontRecords = new ArrayList<FontRecord>();

		public void processRecord(Record record) {
			switch (record.getSid()) {

			case BoundSheetRecord.sid:
				BoundSheetRecord bsr = (BoundSheetRecord) record;
				sheets.add(new SheetData(bsr.getSheetname()));
				break;

			case SSTRecord.sid:
				sstrec = (SSTRecord) record;
				break;

			case FontRecord.sid:
				FontRecord frec = (FontRecord) record;
				// System.out.printf("FontRecord %d: %s (%d)\n",
				// fontRecords.size(),
				// frec.getFontName(), frec.getBoldWeight());
				fontRecords.add(frec);
				break;

			case ExtendedFormatRecord.sid:
				ExtendedFormatRecord xfr = (ExtendedFormatRecord) record;
				xfRecords.add(xfr);
				break;

			case BOFRecord.sid:
				BOFRecord bof = (BOFRecord) record;
				if (bof.getType() == BOFRecord.TYPE_WORKSHEET)
					curSheetIndex++;
				break;

			case NumberRecord.sid:
				NumberRecord numrec = (NumberRecord) record;

				double value = numrec.getValue();
				if ((value == Math.floor(value)) && !Double.isInfinite(value))
					addCellToSheet(sheets.get(curSheetIndex), numrec,
							Integer.toString((int) numrec.getValue()));
				else
					addCellToSheet(sheets.get(curSheetIndex), numrec,
							Double.toString(numrec.getValue()));
				break;

			case LabelRecord.sid:
				LabelRecord lrec = (LabelRecord) record;
				addCellToSheet(sheets.get(curSheetIndex), lrec, lrec.getValue());
				break;

			case LabelSSTRecord.sid:
				LabelSSTRecord lsstrec = (LabelSSTRecord) record;
				addCellToSheet(sheets.get(curSheetIndex), lsstrec,
						sstrec.getString(lsstrec.getSSTIndex()).toString());
				break;
			}
		}

		public void addCellToSheet(SheetData sheet, CellValueRecordInterface cell, String data) {

			ExtendedFormatRecord format = xfRecords.get(cell.getXFIndex());

			try {
				FontRecord font = fontRecords.get(format.getFontIndex() - 1);

				sheet.addCell(new CellData(cell.getColumn() + 1, cell.getRow() + 1, data, font
						.getBoldWeight()));
			} catch (ArrayIndexOutOfBoundsException e) {
				// System.err.printf("ArrayIndexOutOfBoundsException at sheet %s, cell (%d,%d)\n",
				// sheet.getSheetName(), cell.getColumn() + 1, cell.getRow() +
				// 1);
			}
		}

		public ArrayList<SheetData> getData() {
			return sheets;
		}
	}

}
