/*
 * Copyright 2002-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package example;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class Saml2LoginApplicationITests {

	static final String SIGNED_RESPONSE = "PHNhbWxwOlJlc3BvbnNlIHhtbG5zOnNhbWxwPSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6cHJvdG9jb2wiIHhtbG5zOnNhbWw9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphc3NlcnRpb24iIElEPSJfNWJhOGEyMGJlMDE3NzIxZTFiYjhmYmIwNGUxNzRhNDNjN2FiOTFjYTkxIiBWZXJzaW9uPSIyLjAiIElzc3VlSW5zdGFudD0iMjAyMS0xMC0xMlQxNDozMzowOFoiIERlc3RpbmF0aW9uPSJodHRwOi8vbG9jYWxob3N0OjgwODAvbG9naW4vc2FtbDIvc3NvIiBJblJlc3BvbnNlVG89IkFSUTI3OTZiZGMtOWJiOC00OTNmLWI3ZTMtMWE2MjI5ZjFiN2U0Ij48c2FtbDpJc3N1ZXI+aHR0cHM6Ly9zaW1wbGVzYW1scGhwLmFwcHMucGNmb25lLmlvL3NhbWwyL2lkcC9tZXRhZGF0YS5waHA8L3NhbWw6SXNzdWVyPjxkczpTaWduYXR1cmUgeG1sbnM6ZHM9Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyMiPgogIDxkczpTaWduZWRJbmZvPjxkczpDYW5vbmljYWxpemF0aW9uTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+CiAgICA8ZHM6U2lnbmF0dXJlTWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxkc2lnLW1vcmUjcnNhLXNoYTI1NiIvPgogIDxkczpSZWZlcmVuY2UgVVJJPSIjXzViYThhMjBiZTAxNzcyMWUxYmI4ZmJiMDRlMTc0YTQzYzdhYjkxY2E5MSI+PGRzOlRyYW5zZm9ybXM+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDAvMDkveG1sZHNpZyNlbnZlbG9wZWQtc2lnbmF0dXJlIi8+PGRzOlRyYW5zZm9ybSBBbGdvcml0aG09Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvMTAveG1sLWV4Yy1jMTRuIyIvPjwvZHM6VHJhbnNmb3Jtcz48ZHM6RGlnZXN0TWV0aG9kIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8wNC94bWxlbmMjc2hhMjU2Ii8+PGRzOkRpZ2VzdFZhbHVlPkNzM3U4UXRsSXlaMFZrRXRaU0FhL1VyUFJPbnRxT2syYVhaUmM0cVJSM2M9PC9kczpEaWdlc3RWYWx1ZT48L2RzOlJlZmVyZW5jZT48L2RzOlNpZ25lZEluZm8+PGRzOlNpZ25hdHVyZVZhbHVlPm9ZdW1idWQxYmU0ZWZJTUNGVGdqVG4ydGdOSUJpZlc4L3BRZGJtcUVCVzNaM29VVVdTWnEraDQvd3VWdlBEQ2l5R2J4d28yQ3lhS3JzdW1sUG5rWk9sR3lrdUYzbkFPRkJSaEd1OEdobWdObktiZkVuWXVzQldlOFdiZkFpYXJ6UUQwWHBOYmdsSHpsaE96Umk5ZnR1YkNjbTVQbGFEQytpVW1BbXYxT2ZSYnA5MncrVGhUbEtwaTQzeDNsQW1vRkk0ZlJQbUN2R1FGd0JYSEM5bG1KNGU1MUgwMm1nS2NTTTlnaGFLSk9DeTRxRVVjMVk3RE5xOTM4UHllcEZzVzRzc0I1VmJRalpxTjFBcmZOa0RTOTRKOXBvZFNsSktBcnlyKy9GZml6a0VndmJneVhwajgwclVLcThtNHN3SkE1KzV4NUZMY21xZ2UxR2FRRTVUQUxoUT09PC9kczpTaWduYXR1cmVWYWx1ZT4KPGRzOktleUluZm8+PGRzOlg1MDlEYXRhPjxkczpYNTA5Q2VydGlmaWNhdGU+TUlJRUV6Q0NBdnVnQXdJQkFnSUpBSWMxcXpMcnYrNW5NQTBHQ1NxR1NJYjNEUUVCQ3dVQU1JR2ZNUXN3Q1FZRFZRUUdFd0pWVXpFTE1Ba0dBMVVFQ0F3Q1EwOHhGREFTQmdOVkJBY01DME5oYzNSc1pTQlNiMk5yTVJ3d0dnWURWUVFLREJOVFlXMXNJRlJsYzNScGJtY2dVMlZ5ZG1WeU1Rc3dDUVlEVlFRTERBSkpWREVnTUI0R0ExVUVBd3dYYzJsdGNHeGxjMkZ0YkhCb2NDNWpabUZ3Y0hNdWFXOHhJREFlQmdrcWhraUc5dzBCQ1FFV0VXWm9ZVzVwYTBCd2FYWnZkR0ZzTG1sdk1CNFhEVEUxTURJeU16SXlORFV3TTFvWERUSTFNREl5TWpJeU5EVXdNMW93Z1o4eEN6QUpCZ05WQkFZVEFsVlRNUXN3Q1FZRFZRUUlEQUpEVHpFVU1CSUdBMVVFQnd3TFEyRnpkR3hsSUZKdlkyc3hIREFhQmdOVkJBb01FMU5oYld3Z1ZHVnpkR2x1WnlCVFpYSjJaWEl4Q3pBSkJnTlZCQXNNQWtsVU1TQXdIZ1lEVlFRRERCZHphVzF3YkdWellXMXNjR2h3TG1ObVlYQndjeTVwYnpFZ01CNEdDU3FHU0liM0RRRUpBUllSWm1oaGJtbHJRSEJwZG05MFlXd3VhVzh3Z2dFaU1BMEdDU3FHU0liM0RRRUJBUVVBQTRJQkR3QXdnZ0VLQW9JQkFRQzRjbjYyRTF4THFwTjM0UG1icktCYmtPWEZqeldnSjliK3BYdWFSZnQ2QTMzOXV1SVFlb2VINXFlU0tSVlRsMzJMMGdkejJaaXZMd1pYVytjcXZmdFZXMXR2RUh2ekpGeXhlVFczZkNVZUNRc2ViTG5BMnFSYTA3Umt4VG82TmYyNDRtV1dSRG9kY29IRWZEVVNieGZUWjZJRXhTb2pTSVUyUm5ENldsbFlXRmREMUdGcEJKT21RQjhyQWM4d0pJQmRIRmRRblg4VHRsN2haNnJ0Z3FFWU16WVZNdUoyRjJyMUhTVTF6U0F2d3BkWVA2clJHRlJKRWZkQTltbTNXS2ZOTFNjNWNsanowWC9UWHkwdlZsQVY5NWw5cWNmRnpQbXJrTklzdDlGWlN3cHZCNDlMeUFWa2UwNEZRUFB3TGdWSDRncGhpSkgzanZaN0krSjVsUzhWQWdNQkFBR2pVREJPTUIwR0ExVWREZ1FXQkJUVHlQNkNjNUhsQko1K3VjVkN3R2M1b2dLTkd6QWZCZ05WSFNNRUdEQVdnQlRUeVA2Q2M1SGxCSjUrdWNWQ3dHYzVvZ0tOR3pBTUJnTlZIUk1FQlRBREFRSC9NQTBHQ1NxR1NJYjNEUUVCQ3dVQUE0SUJBUUF2TVM0RVFlUC9pcFY0ak9HNWxPNi90WUNiL2lKZUFkdU9uUmhrSmswRGJYMzI5bERMWmhUVEwveC93LzltdUNWY3ZMcnpFcDZQTitWV2Z3NUU1Rld0Wk4weWhHdFA5Uit2Wm5yVitvYzJ6R0Qrbm8xL3lTRk9lM0VpSkNPNWRlaHhLallFbUJSdjVzVS9MWkZLWnBvektOL0JNRWE2Q3FMdXhiemI3eWt4VnI3RVZGWHdsdFB4ekU5VG1MOU9BQ05OeUY1ZUpIV01STWxsYXJVdmtjWGxoNHB1eDRrczllNnpWOURRQnkyemRzOWYxSTNxeGcwZVg2Sm5HclhpL1ppQ1QrbEpnVmUzWkZYaWVqaUxBaUtCMDRzWFczdGkwTFczbHgxM1kxWWxRNC90bHBnVGdmSUp4S1Y2bnlQaUxvSzBueXdiTWQrdnBBaXJEdDJPYytoazwvZHM6WDUwOUNlcnRpZmljYXRlPjwvZHM6WDUwOURhdGE+PC9kczpLZXlJbmZvPjwvZHM6U2lnbmF0dXJlPjxzYW1scDpTdGF0dXM+PHNhbWxwOlN0YXR1c0NvZGUgVmFsdWU9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpzdGF0dXM6U3VjY2VzcyIvPjwvc2FtbHA6U3RhdHVzPjxzYW1sOkFzc2VydGlvbiB4bWxuczp4c2k9Imh0dHA6Ly93d3cudzMub3JnLzIwMDEvWE1MU2NoZW1hLWluc3RhbmNlIiB4bWxuczp4cz0iaHR0cDovL3d3dy53My5vcmcvMjAwMS9YTUxTY2hlbWEiIElEPSJfYTU2NmRiYzE1Yjg0ZDUxNTFmOWJjODgwOTYzOWUyMWI3ZTMwNjg4MGFmIiBWZXJzaW9uPSIyLjAiIElzc3VlSW5zdGFudD0iMjAyMS0xMC0xMlQxNDozMzowOFoiPjxzYW1sOklzc3Vlcj5odHRwczovL3NpbXBsZXNhbWxwaHAuYXBwcy5wY2ZvbmUuaW8vc2FtbDIvaWRwL21ldGFkYXRhLnBocDwvc2FtbDpJc3N1ZXI+PGRzOlNpZ25hdHVyZSB4bWxuczpkcz0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnIyI+CiAgPGRzOlNpZ25lZEluZm8+PGRzOkNhbm9uaWNhbGl6YXRpb25NZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzEwL3htbC1leGMtYzE0biMiLz4KICAgIDxkczpTaWduYXR1cmVNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGRzaWctbW9yZSNyc2Etc2hhMjU2Ii8+CiAgPGRzOlJlZmVyZW5jZSBVUkk9IiNfYTU2NmRiYzE1Yjg0ZDUxNTFmOWJjODgwOTYzOWUyMWI3ZTMwNjg4MGFmIj48ZHM6VHJhbnNmb3Jtcz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMC8wOS94bWxkc2lnI2VudmVsb3BlZC1zaWduYXR1cmUiLz48ZHM6VHJhbnNmb3JtIEFsZ29yaXRobT0iaHR0cDovL3d3dy53My5vcmcvMjAwMS8xMC94bWwtZXhjLWMxNG4jIi8+PC9kczpUcmFuc2Zvcm1zPjxkczpEaWdlc3RNZXRob2QgQWxnb3JpdGhtPSJodHRwOi8vd3d3LnczLm9yZy8yMDAxLzA0L3htbGVuYyNzaGEyNTYiLz48ZHM6RGlnZXN0VmFsdWU+STdLWlpiY0V2R2xrb2NWSWtuT0tOQ1MwbXVvbmlDU1ViYzBwQ2NOR3phRT08L2RzOkRpZ2VzdFZhbHVlPjwvZHM6UmVmZXJlbmNlPjwvZHM6U2lnbmVkSW5mbz48ZHM6U2lnbmF0dXJlVmFsdWU+cjdkMzJtUzB0MXNJaXNEZzcway9xdGVQNWVFb1BtV2tGT3pGQytjaTdPZmNPUmZ3WmZpd0pwSlVKb2RjRE1TZjlRRjVZOFQ1ckp2VXp3UFZwZHBnVFVzUmpQaUQ4K0JxWS9QMnhQc1didE5wYks3cEhjZmlDQWU5aTAvc2o4MktZajVUZlozMlBGV1RGSmVOSVdtUkhxNnlLRVRySmpmM0UvNVBsb0g1WmMyMFljZUMxRkloR0M1akpjSXl3cXVST01WZ1NGdWhVZ0VVaHJMOVpHVUtYdE5KL2dva0JobjloTjFMM1lCdnRYYm9kUExxN0NlUXpvT1pTWDV6bllRbTR4elhiSnN1OHVKOWEva3haUnNPd2JnUXZOcU5sL2t5akgvVTBxVk9HVjJ3ZzhDemhXMy9PN2RRb3VPQVNPV1pxVitpU3dYdFIweE95Zm1kQnlJZXRBPT08L2RzOlNpZ25hdHVyZVZhbHVlPgo8ZHM6S2V5SW5mbz48ZHM6WDUwOURhdGE+PGRzOlg1MDlDZXJ0aWZpY2F0ZT5NSUlFRXpDQ0F2dWdBd0lCQWdJSkFJYzFxekxydis1bk1BMEdDU3FHU0liM0RRRUJDd1VBTUlHZk1Rc3dDUVlEVlFRR0V3SlZVekVMTUFrR0ExVUVDQXdDUTA4eEZEQVNCZ05WQkFjTUMwTmhjM1JzWlNCU2IyTnJNUnd3R2dZRFZRUUtEQk5UWVcxc0lGUmxjM1JwYm1jZ1UyVnlkbVZ5TVFzd0NRWURWUVFMREFKSlZERWdNQjRHQTFVRUF3d1hjMmx0Y0d4bGMyRnRiSEJvY0M1alptRndjSE11YVc4eElEQWVCZ2txaGtpRzl3MEJDUUVXRVdab1lXNXBhMEJ3YVhadmRHRnNMbWx2TUI0WERURTFNREl5TXpJeU5EVXdNMW9YRFRJMU1ESXlNakl5TkRVd00xb3dnWjh4Q3pBSkJnTlZCQVlUQWxWVE1Rc3dDUVlEVlFRSURBSkRUekVVTUJJR0ExVUVCd3dMUTJGemRHeGxJRkp2WTJzeEhEQWFCZ05WQkFvTUUxTmhiV3dnVkdWemRHbHVaeUJUWlhKMlpYSXhDekFKQmdOVkJBc01Ba2xVTVNBd0hnWURWUVFEREJkemFXMXdiR1Z6WVcxc2NHaHdMbU5tWVhCd2N5NXBiekVnTUI0R0NTcUdTSWIzRFFFSkFSWVJabWhoYm1sclFIQnBkbTkwWVd3dWFXOHdnZ0VpTUEwR0NTcUdTSWIzRFFFQkFRVUFBNElCRHdBd2dnRUtBb0lCQVFDNGNuNjJFMXhMcXBOMzRQbWJyS0Jia09YRmp6V2dKOWIrcFh1YVJmdDZBMzM5dXVJUWVvZUg1cWVTS1JWVGwzMkwwZ2R6Mlppdkx3WlhXK2NxdmZ0VlcxdHZFSHZ6SkZ5eGVUVzNmQ1VlQ1FzZWJMbkEycVJhMDdSa3hUbzZOZjI0NG1XV1JEb2Rjb0hFZkRVU2J4ZlRaNklFeFNvalNJVTJSbkQ2V2xsWVdGZEQxR0ZwQkpPbVFCOHJBYzh3SklCZEhGZFFuWDhUdGw3aFo2cnRncUVZTXpZVk11SjJGMnIxSFNVMXpTQXZ3cGRZUDZyUkdGUkpFZmRBOW1tM1dLZk5MU2M1Y2xqejBYL1RYeTB2VmxBVjk1bDlxY2ZGelBtcmtOSXN0OUZaU3dwdkI0OUx5QVZrZTA0RlFQUHdMZ1ZINGdwaGlKSDNqdlo3SStKNWxTOFZBZ01CQUFHalVEQk9NQjBHQTFVZERnUVdCQlRUeVA2Q2M1SGxCSjUrdWNWQ3dHYzVvZ0tOR3pBZkJnTlZIU01FR0RBV2dCVFR5UDZDYzVIbEJKNSt1Y1ZDd0djNW9nS05HekFNQmdOVkhSTUVCVEFEQVFIL01BMEdDU3FHU0liM0RRRUJDd1VBQTRJQkFRQXZNUzRFUWVQL2lwVjRqT0c1bE82L3RZQ2IvaUplQWR1T25SaGtKazBEYlgzMjlsRExaaFRUTC94L3cvOW11Q1ZjdkxyekVwNlBOK1ZXZnc1RTVGV3RaTjB5aEd0UDlSK3ZabnJWK29jMnpHRCtubzEveVNGT2UzRWlKQ081ZGVoeEtqWUVtQlJ2NXNVL0xaRktacG96S04vQk1FYTZDcUx1eGJ6Yjd5a3hWcjdFVkZYd2x0UHh6RTlUbUw5T0FDTk55RjVlSkhXTVJNbGxhclV2a2NYbGg0cHV4NGtzOWU2elY5RFFCeTJ6ZHM5ZjFJM3F4ZzBlWDZKbkdyWGkvWmlDVCtsSmdWZTNaRlhpZWppTEFpS0IwNHNYVzN0aTBMVzNseDEzWTFZbFE0L3RscGdUZ2ZJSnhLVjZueVBpTG9LMG55d2JNZCt2cEFpckR0Mk9jK2hrPC9kczpYNTA5Q2VydGlmaWNhdGU+PC9kczpYNTA5RGF0YT48L2RzOktleUluZm8+PC9kczpTaWduYXR1cmU+PHNhbWw6U3ViamVjdD48c2FtbDpOYW1lSUQgU1BOYW1lUXVhbGlmaWVyPSJodHRwOi8vbG9jYWxob3N0OjgwODAvc2FtbDIvbWV0YWRhdGEiIEZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6MS4xOm5hbWVpZC1mb3JtYXQ6ZW1haWxBZGRyZXNzIj50ZXN0dXNlckBzcHJpbmcuc2VjdXJpdHkuc2FtbDwvc2FtbDpOYW1lSUQ+PHNhbWw6U3ViamVjdENvbmZpcm1hdGlvbiBNZXRob2Q9InVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDpjbTpiZWFyZXIiPjxzYW1sOlN1YmplY3RDb25maXJtYXRpb25EYXRhIE5vdE9uT3JBZnRlcj0iMjA1My0wNi0yMFQxNjoxOTo0OFoiIFJlY2lwaWVudD0iaHR0cDovL2xvY2FsaG9zdDo4MDgwL2xvZ2luL3NhbWwyL3NzbyIgSW5SZXNwb25zZVRvPSJBUlEyNzk2YmRjLTliYjgtNDkzZi1iN2UzLTFhNjIyOWYxYjdlNCIvPjwvc2FtbDpTdWJqZWN0Q29uZmlybWF0aW9uPjwvc2FtbDpTdWJqZWN0PjxzYW1sOkNvbmRpdGlvbnMgTm90QmVmb3JlPSIyMDIxLTEwLTEyVDE0OjMyOjM4WiIgTm90T25PckFmdGVyPSIyMDUzLTA2LTIwVDE2OjE5OjQ4WiI+PHNhbWw6QXVkaWVuY2VSZXN0cmljdGlvbj48c2FtbDpBdWRpZW5jZT5odHRwOi8vbG9jYWxob3N0OjgwODAvc2FtbDIvbWV0YWRhdGE8L3NhbWw6QXVkaWVuY2U+PC9zYW1sOkF1ZGllbmNlUmVzdHJpY3Rpb24+PC9zYW1sOkNvbmRpdGlvbnM+PHNhbWw6QXV0aG5TdGF0ZW1lbnQgQXV0aG5JbnN0YW50PSIyMDIxLTEwLTEyVDE0OjMzOjA4WiIgU2Vzc2lvbk5vdE9uT3JBZnRlcj0iMjAyMS0xMC0xMlQyMjozMzowOFoiIFNlc3Npb25JbmRleD0iXzlkNTg3ODM2YWFmMTBjNmM2ODhmMTc0YTZkYzYyNDZlZjEyNTQ4Mzk2MyI+PHNhbWw6QXV0aG5Db250ZXh0PjxzYW1sOkF1dGhuQ29udGV4dENsYXNzUmVmPnVybjpvYXNpczpuYW1lczp0YzpTQU1MOjIuMDphYzpjbGFzc2VzOlBhc3N3b3JkUHJvdGVjdGVkVHJhbnNwb3J0PC9zYW1sOkF1dGhuQ29udGV4dENsYXNzUmVmPjwvc2FtbDpBdXRobkNvbnRleHQ+PC9zYW1sOkF1dGhuU3RhdGVtZW50PjxzYW1sOkF0dHJpYnV0ZVN0YXRlbWVudD48c2FtbDpBdHRyaWJ1dGUgTmFtZT0idWlkIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OmJhc2ljIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4c2k6dHlwZT0ieHM6c3RyaW5nIj50ZXN0dXNlckBzcHJpbmcuc2VjdXJpdHkuc2FtbDwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjxzYW1sOkF0dHJpYnV0ZSBOYW1lPSJlZHVQZXJzb25BZmZpbGlhdGlvbiIgTmFtZUZvcm1hdD0idXJuOm9hc2lzOm5hbWVzOnRjOlNBTUw6Mi4wOmF0dHJuYW1lLWZvcm1hdDpiYXNpYyI+PHNhbWw6QXR0cmlidXRlVmFsdWUgeHNpOnR5cGU9InhzOnN0cmluZyI+bWVtYmVyPC9zYW1sOkF0dHJpYnV0ZVZhbHVlPjxzYW1sOkF0dHJpYnV0ZVZhbHVlIHhzaTp0eXBlPSJ4czpzdHJpbmciPnVzZXI8L3NhbWw6QXR0cmlidXRlVmFsdWU+PC9zYW1sOkF0dHJpYnV0ZT48c2FtbDpBdHRyaWJ1dGUgTmFtZT0iZW1haWxBZGRyZXNzIiBOYW1lRm9ybWF0PSJ1cm46b2FzaXM6bmFtZXM6dGM6U0FNTDoyLjA6YXR0cm5hbWUtZm9ybWF0OmJhc2ljIj48c2FtbDpBdHRyaWJ1dGVWYWx1ZSB4c2k6dHlwZT0ieHM6c3RyaW5nIj50ZXN0dXNlckBzcHJpbmcuc2VjdXJpdHkuc2FtbDwvc2FtbDpBdHRyaWJ1dGVWYWx1ZT48L3NhbWw6QXR0cmlidXRlPjwvc2FtbDpBdHRyaWJ1dGVTdGF0ZW1lbnQ+PC9zYW1sOkFzc2VydGlvbj48L3NhbWxwOlJlc3BvbnNlPg==";

	static final Map<String, List<Object>> USER_ATTRIBUTES = new LinkedHashMap<>();

	static {
		USER_ATTRIBUTES.put("uid", Arrays.asList("testuser@spring.security.saml"));
		USER_ATTRIBUTES.put("eduPersonAffiliation", Arrays.asList("member", "user"));
		USER_ATTRIBUTES.put("emailAddress", Arrays.asList("testuser@spring.security.saml"));
	}

	@Autowired
	MockMvc mvc;

	@Autowired
	WebClient webClient;

	@BeforeEach
	void setup() {
		this.webClient.getCookieManager().clearCookies();
	}

	@Test
	void indexWhenSamlResponseThenShowsUserInformation() throws Exception {
		HttpSession session = this.mvc.perform(get("http://localhost:8080/")).andExpect(status().is3xxRedirection())
				.andExpect(redirectedUrl("http://localhost:8080/saml2/authenticate/metadata")).andReturn().getRequest()
				.getSession();

		this.mvc.perform(post("http://localhost:8080/login/saml2/sso").param("SAMLResponse", SIGNED_RESPONSE)
				.session((MockHttpSession) session)).andExpect(redirectedUrl("http://localhost:8080/"));

		this.mvc.perform(get("http://localhost:8080/").session((MockHttpSession) session))
				.andExpect(model().attribute("emailAddress", "testuser@spring.security.saml"))
				.andExpect(model().attribute("userAttributes", USER_ATTRIBUTES));
	}

	@Test
	void authenticationAttemptWhenValidThenShowsUserEmailAddress() throws Exception {
		HtmlPage relyingParty = performLogin();
		assertThat(relyingParty.asNormalizedText()).contains("You're email address is testuser@spring.security.saml");
	}

	@Test
	void logoutWhenRelyingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		HtmlPage relyingParty = performLogin();
		HtmlElement rpLogoutButton = relyingParty.getHtmlElementById("rp_logout_button");
		HtmlPage loginPage = rpLogoutButton.click();
		assertThat(loginPage.getUrl().getFile()).isEqualTo("/login?logout");
	}

	@Test
	void logoutWhenAssertingPartyInitiatedLogoutThenLoginPageWithLogoutParam() throws Exception {
		HtmlPage relyingParty = performLogin();
		HtmlElement apLogoutButton = relyingParty.getHtmlElementById("ap_logout_button");
		HtmlPage loginPage = apLogoutButton.click();
		assertThat(loginPage.getUrl().getFile()).isEqualTo("/login?logout");
	}

	private HtmlPage performLogin() throws IOException {
		HtmlPage assertingParty = this.webClient.getPage("/");
		HtmlForm form = assertingParty.getFormByName("f");
		HtmlInput username = form.getInputByName("username");
		HtmlInput password = form.getInputByName("password");
		HtmlSubmitInput submit = assertingParty.getHtmlElementById("submit_button");
		username.setValueAttribute("user");
		password.setValueAttribute("password");
		return submit.click();
	}

}
