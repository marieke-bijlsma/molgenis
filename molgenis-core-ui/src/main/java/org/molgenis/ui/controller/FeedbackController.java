package org.molgenis.ui.controller;

import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.validation.Valid;

import org.apache.log4j.Logger;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.molgenis.framework.server.MolgenisSettings;
import org.molgenis.framework.ui.MolgenisPluginController;
import org.molgenis.omx.auth.MolgenisUser;
import org.molgenis.security.core.utils.SecurityUtils;
import org.molgenis.security.user.MolgenisUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Controller that handles feedback page requests.
 * The user can fill in this feedback form to send a mail to the app's administrators.
 */
@Controller
@RequestMapping(FeedbackController.URI)
public class FeedbackController extends AbstractStaticContentController
{
	public static final String ID = "feedback";
	public static final String URI = MolgenisPluginController.PLUGIN_URI_PREFIX + ID;
	public static final String MESSAGING_EXCEPTION_MESSAGE = "Unfortunately, we were unable to create an email message for the feedback you specified.";
	public static final String MAIL_AUTHENTICATION_EXCEPTION_MESSAGE = "Unfortunately, we were unable to send the mail containing your feedback.<br/>Please contact the administrator.";
	public static final String MAIL_SEND_EXCEPTION_MESSAGE = MAIL_AUTHENTICATION_EXCEPTION_MESSAGE;
	private static final Logger LOGGER = Logger.getLogger(MolgenisPluginController.class);

	@Autowired
	private MolgenisUserService molgenisUserService;

	@Autowired
	private MolgenisSettings molgenisSettings;

	@Autowired
	private JavaMailSender mailSender;

	public FeedbackController()
	{
		super(ID, URI);
	}

	/**
	 * Serves feedback form.
	 */
	@RequestMapping(method = RequestMethod.GET)
	public String init(final Model model)
	{
		super.init(model);
		model.addAttribute("adminEmails", molgenisUserService.getSuEmailAddresses());
		if (SecurityUtils.currentUserIsAuthenticated())
		{
			MolgenisUser currentUser = molgenisUserService.getUser(SecurityUtils.getCurrentUsername());
			model.addAttribute("userName", getFormattedName(currentUser));
			model.addAttribute("userEmail", currentUser.getEmail());
		}
		return "view-feedback";
	}

	/**
	 * Handles feedback form submission.
	 */
	@RequestMapping(method = RequestMethod.POST)
	public String submitFeedback(@Valid FeedbackForm form)
	{
		try
		{
			LOGGER.info("Sending feedback:" + form);
			MimeMessage message = createFeedbackMessage(form);
			mailSender.send(message);
			form.setSubmitted(true);
		}
		catch (MessagingException e)
		{
			LOGGER.warn("Unable to create mime message for feedback form.", e);
			form.setErrorMessage(MESSAGING_EXCEPTION_MESSAGE);
		}
		catch (MailAuthenticationException e)
		{
			LOGGER.error("Error authenticating with email server.", e);
			form.setErrorMessage(MAIL_AUTHENTICATION_EXCEPTION_MESSAGE);
		}
		catch (MailSendException e)
		{
			LOGGER.error("Error sending mail", e);
			form.setErrorMessage(MAIL_SEND_EXCEPTION_MESSAGE);
		}
		return "view-feedback";
	}

	/**
	 * Creates a MimeMessage based on a FeedbackForm.
	 * 
	 */
	private MimeMessage createFeedbackMessage(FeedbackForm form) throws MessagingException
	{
		MimeMessage message = mailSender.createMimeMessage();
		MimeMessageHelper helper = new MimeMessageHelper(message, false);
		helper.setTo(molgenisUserService.getSuEmailAddresses().toArray(new String[0]));
		if (form.hasEmail())
		{
			helper.setCc(form.getEmail());
			helper.setReplyTo(form.getEmail());
		}
		else
		{
			helper.setReplyTo("no-reply@molgenis.org");
		}
		String appName = molgenisSettings.getProperty("app.name", "molgenis");
		helper.setSubject(String.format("[feedback-%s] %s", appName, form.getSubject()));
		helper.setText(String.format("Feedback from %s:\n\n%s", form.getFrom(), form.getFeedback()));
		return message;
	}

	/**
	 * Formats a MolgenisUser's name.
	 * 
	 * @return String containing the user's first name, middle names and last name.
	 */
	private static String getFormattedName(MolgenisUser user)
	{
		List<String> parts = new ArrayList<String>();
		if (user.getTitle() != null)
		{
			parts.add(user.getTitle());
		}
		if (user.getFirstName() != null)
		{
			parts.add(user.getFirstName());
		}
		if (user.getMiddleNames() != null)
		{
			parts.add(user.getMiddleNames());
		}
		if (user.getLastName() != null)
		{
			parts.add(user.getLastName());
		}

		if (parts.isEmpty())
		{
			return null;
		}
		else
		{
			return StringUtils.collectionToDelimitedString(parts, " ");
		}
	}

	/**
	 * Bean for the feedback form data.
	 */
	public static class FeedbackForm
	{
		private String name;
		private String email;
		private String subject;
		private String feedback;
		private boolean submitted = false;
		private String errorMessage;

		public String getName()
		{
			return name;
		}

		public boolean hasName()
		{
			return name != null && !name.trim().isEmpty();
		}

		public void setName(String name)
		{
			this.name = name;
		}

		@Email
		public String getEmail()
		{
			return email;
		}

		public void setEmail(String email)
		{
			this.email = email;
		}

		public boolean hasEmail()
		{
			return email != null && !email.trim().isEmpty();
		}

		public String getFrom()
		{
			StringBuilder result = new StringBuilder();
			if (!hasName() && !hasEmail())
			{
				result.append("Anonymous");
			}
			else
			{
				if (hasName())
				{
					result.append(getName());
				}
				if (hasEmail())
				{
					if (hasName())
					{
						result.append(String.format(" (%s)", getEmail()));
					}
					else
					{
						result.append(getEmail());
					}
				}
			}
			return result.toString();
		}

		public String getSubject()
		{
			if (subject == null || subject.trim().isEmpty())
			{
				return "<no subject>";
			}
			return subject;
		}

		public void setSubject(String subject)
		{
			this.subject = subject;
		}

		@NotBlank
		public String getFeedback()
		{
			return feedback;
		}

		public void setFeedback(String feedback)
		{
			this.feedback = feedback;
		}

		public void setSubmitted(boolean b)
		{
			this.submitted = b;
		}

		public boolean isSubmitted()
		{
			return submitted;
		}

		public String getErrorMessage()
		{
			return errorMessage;
		}

		public void setErrorMessage(String errorMessage)
		{
			this.errorMessage = errorMessage;
		}

		@Override
		public String toString()
		{
			StringBuilder builder = new StringBuilder();
			builder.append("[From: ");
			builder.append(getFrom());
			builder.append("\nSubject: ");
			builder.append(getSubject());
			builder.append("\nBody: ");
			builder.append(getFeedback());
			builder.append(']');
			return builder.toString();
		}
	}
}