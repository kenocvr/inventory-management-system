import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { JhiDataUtils } from 'ng-jhipster';
import { ViewEncapsulation } from '@angular/core';

import { IItem } from 'app/shared/model/item.model';
import { Console } from 'inspector';

@Component({
  selector: 'jhi-item-detail',
  templateUrl: './item-detail.component.html',
  encapsulation: ViewEncapsulation.None
})
export class ItemDetailComponent implements OnInit {
  item: IItem | null = null;
  availableDevices?: MediaDeviceInfo[];
  currentDevice?: MediaDeviceInfo;
  hasDevices?: boolean;
  hasPermission?: boolean;
  guestExist?: boolean;
  qrResult?: string;

  constructor(protected dataUtils: JhiDataUtils, protected activatedRoute: ActivatedRoute) {}

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ item }) => (this.item = item));
  }

  byteSize(base64String: string): string {
    return this.dataUtils.byteSize(base64String);
  }

  openFile(contentType: string, base64String: string): void {
    this.dataUtils.openFile(contentType, base64String);
  }

  previousState(): void {
    window.history.back();
  }

  // Scans the QR code
  onCodeResult(resultString: string): void {
    /* eslint-disable no-console */
    // console.log(JSON.parse(resultString));
    console.log(resultString);

    this.qrResult = resultString;
  }

  onHasPermission(has: boolean): void {
    this.hasPermission = has;
  }
}
