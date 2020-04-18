import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';

import './vendor';
import { InventorySharedModule } from 'app/shared/shared.module';
import { InventoryCoreModule } from 'app/core/core.module';
import { InventoryAppRoutingModule } from './app-routing.module';
import { InventoryHomeModule } from './home/home.module';
import { InventoryEntityModule } from './entities/entity.module';

import { ZXingScannerModule } from '@zxing/ngx-scanner';

import { BarecodeScannerLivestreamModule } from 'ngx-barcode-scanner';
// jhipster-needle-angular-add-module-import JHipster will add new module here

import { MainComponent } from './layouts/main/main.component';
import { NavbarComponent } from './layouts/navbar/navbar.component';
import { FooterComponent } from './layouts/footer/footer.component';
import { PageRibbonComponent } from './layouts/profiles/page-ribbon.component';
import { ActiveMenuDirective } from './layouts/navbar/active-menu.directive';
import { ErrorComponent } from './layouts/error/error.component';
import { NgQrScannerModule } from 'angular2-qrscanner';

@NgModule({
  imports: [
    BrowserModule,
    InventorySharedModule,
    InventoryCoreModule,
    InventoryHomeModule,
    // jhipster-needle-angular-add-module JHipster will add new module here
    InventoryEntityModule,
    InventoryAppRoutingModule,
    ZXingScannerModule,
    BarecodeScannerLivestreamModule,
    NgQrScannerModule
  ],
  declarations: [MainComponent, NavbarComponent, ErrorComponent, PageRibbonComponent, ActiveMenuDirective, FooterComponent],
  bootstrap: [MainComponent]
})
export class InventoryAppModule {}
