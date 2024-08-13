import {Component} from "@angular/core";
import {APP_BASE_HREF, CommonModule} from "@angular/common";
import {RouterOutlet} from "@angular/router";

@Component({
  selector: 'preview-panel',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './preview-panel.component.html',
  styleUrl: './preview-panel.component.css'
})
export class PreviewPanelComponent {

}
